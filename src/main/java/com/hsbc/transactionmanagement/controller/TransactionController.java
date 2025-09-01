package com.hsbc.transactionmanagement.controller;

import com.hsbc.transactionmanagement.constant.ErrorMessages;
import com.hsbc.transactionmanagement.entity.Transaction;
import com.hsbc.transactionmanagement.exception.DuplicateTransactionException;
import com.hsbc.transactionmanagement.exception.TransactionNotFoundException;
import com.hsbc.transactionmanagement.response.CommonResponse;
import com.hsbc.transactionmanagement.response.PagedResponse;
import com.hsbc.transactionmanagement.service.TransactionService;
import com.hsbc.transactionmanagement.util.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing financial transactions")
public class TransactionController {

    private static final Logger logger = LoggerUtil.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Create a new transaction", description = "Creates a new financial transaction")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Transaction with reference already exists")
    })
    public ResponseEntity<CommonResponse<Transaction>> createTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ID in request will be ignored; it is server-assigned")
            @Valid @RequestBody Transaction transaction) {

        long startTime = System.currentTimeMillis();
        ResponseEntity<CommonResponse<Transaction>> responseEntity;
        Transaction createdTransaction = null;

        // Ensure ID is always system-assigned and not taken from client payload
        logger.warn("If ID is provided in create request, it will be ignored and system-assigned.");

        Transaction inputTransaction  = new Transaction();
        BeanUtils.copyProperties(transaction, inputTransaction, "id", "transactionDate");

        try {
            createdTransaction = transactionService.createTransaction(inputTransaction);
            responseEntity = new ResponseEntity<>(
                    CommonResponse.success(createdTransaction, "Transaction created successfully"),
                    HttpStatus.CREATED);
        } catch (DuplicateTransactionException e) {
            responseEntity =  new ResponseEntity<>(
                    CommonResponse.error(HttpStatus.CONFLICT.value(), ErrorMessages.DUPLICATE_TRANSACTION),
                    HttpStatus.CONFLICT
            );
        }

        logger.info("Created transaction with request: {}, response{}, time cost: {}",
                transaction, responseEntity, System.currentTimeMillis() - startTime);

        return responseEntity;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Returns a transaction by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<CommonResponse<Transaction>> getTransactionById(
            @Parameter(description = "ID of the transaction to be retrieved", example = "1")
            @PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        Transaction transaction = transactionService.getTransactionById(id);

        ResponseEntity<CommonResponse<Transaction>> responseEntity = transaction != null ?
                ResponseEntity.ok(CommonResponse.success(transaction, ErrorMessages.SUCCESS)) :
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(CommonResponse.notFound(ErrorMessages.TRANSACTION_NOT_FOUND + id));
        logger.info("Get transaction by id: {}, response: {}, time cost: {}", id, responseEntity, System.currentTimeMillis() - startTime);
        return responseEntity;
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get transaction by reference", description = "Returns a transaction by its reference number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<CommonResponse<Transaction>> getTransactionByReference(
            @Parameter(description = "Reference number of the transaction to be retrieved", example = "REF123456")
            @PathVariable String reference) {
        long startTime = System.currentTimeMillis();
        Transaction transaction = transactionService.getTransactionByReference(reference);
        ResponseEntity<CommonResponse<Transaction>> responseEntity = transaction != null ?
                ResponseEntity.ok(CommonResponse.success(transaction, ErrorMessages.SUCCESS)) :
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(CommonResponse.notFound(ErrorMessages.TRANSACTION_NOT_FOUND + reference));
        logger.info("Get transaction by reference: {}, response: {}, time cost: {}", reference, responseEntity, System.currentTimeMillis() - startTime);
        return responseEntity;
    }

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Returns a paginated list of transactions with optional filtering")
    public ResponseEntity<CommonResponse<PagedResponse<Transaction>>> getAllTransactions(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort by field", example = "transactionDate")
            @RequestParam(defaultValue = "transactionDate") String sortBy,

            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String direction,

            @Parameter(description = "Filter by category (exact match)")
            @RequestParam(required = false) String category,

            @Parameter(description = "Filter by type (exact match)", example = "DEBIT")
            @RequestParam(required = false) String type) {

        long startTime = System.currentTimeMillis();

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Transaction> transactionPage = transactionService.getAllTransactions(
                category, type, pageable);

        PagedResponse<Transaction> pagedResponse = PagedResponse.fromPage(transactionPage);
        ResponseEntity<CommonResponse<PagedResponse<Transaction>>> responseEntity =
                ResponseEntity.ok(CommonResponse.success(pagedResponse, ErrorMessages.SUCCESS));
        
        logger.info("Get all transactions with filters - page: {}, size: {}, sortBy: {}, " +
                        "direction: {}, category: {}, type: {}. Response: {}, time cost: {}",
                        page, size, sortBy, direction, category, type, responseEntity, System.currentTimeMillis() - startTime);
        return responseEntity;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction", description = "Updates an existing transaction")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<CommonResponse<Transaction>> updateTransaction(
            @Parameter(description = "ID of the transaction to be updated", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Transaction transactionDetails) {

        long startTime = System.currentTimeMillis();
        ResponseEntity<CommonResponse<Transaction>> responseEntity;
        Transaction updatedTransaction = null;

        try {
            updatedTransaction = transactionService.updateTransaction(id, transactionDetails);
            responseEntity =  ResponseEntity.ok(CommonResponse.success(updatedTransaction, ErrorMessages.SUCCESS));
        } catch (TransactionNotFoundException e) {
            responseEntity =  ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.notFound(ErrorMessages.TRANSACTION_NOT_FOUND + id));
        }
        logger.info("Update transaction id: {} with details: {}, response: {}, time cost: {}",
                id, transactionDetails, responseEntity, System.currentTimeMillis() - startTime);
        return responseEntity;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction", description = "Deletes a transaction by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<CommonResponse<Void>> deleteTransaction(
            @Parameter(description = "ID of the transaction to be deleted", example = "1")
            @PathVariable Long id) {

        long startTime = System.currentTimeMillis();

        ResponseEntity<CommonResponse<Void>> responseEntity;
        try {
            transactionService.deleteTransaction(id);
            responseEntity = ResponseEntity.ok(CommonResponse.success(ErrorMessages.SUCCESS));
        } catch (TransactionNotFoundException e) {
            responseEntity =  ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.notFound(ErrorMessages.TRANSACTION_NOT_FOUND + id));
        }
        logger.info("Delete transaction id: {}, response: {}, time cost: {}",
                id, responseEntity, System.currentTimeMillis() - startTime);
        return responseEntity;
    }

    @DeleteMapping("/reference/{reference}")
    @Operation(summary = "Delete a transaction by reference", description = "Deletes a transaction by its reference number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<CommonResponse<Void>> deleteTransactionByReference(
            @Parameter(description = "Reference number of the transaction to be deleted", example = "REF123456")
            @PathVariable String reference) {

        long startTime = System.currentTimeMillis();
        ResponseEntity<CommonResponse<Void>> responseEntity;
        try {
            transactionService.deleteTransactionByReference(reference);
            responseEntity = ResponseEntity.ok(CommonResponse.success(ErrorMessages.SUCCESS));
        } catch (TransactionNotFoundException e) {
            responseEntity =  ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.notFound(ErrorMessages.TRANSACTION_NOT_FOUND + reference));
        }
        logger.info("Delete transaction by reference: {}, response: {}, time cost: {}",
                reference, responseEntity, System.currentTimeMillis() - startTime);
        return responseEntity;
    }
}