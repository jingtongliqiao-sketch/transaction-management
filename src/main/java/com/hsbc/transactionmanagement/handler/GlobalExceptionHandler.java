package com.hsbc.transactionmanagement.handler;

import com.hsbc.transactionmanagement.constant.ErrorMessages;
import com.hsbc.transactionmanagement.exception.DuplicateTransactionException;
import com.hsbc.transactionmanagement.exception.TransactionNotFoundException;
import com.hsbc.transactionmanagement.response.CommonResponse;
import com.hsbc.transactionmanagement.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerUtil.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<CommonResponse<Map<String, String>>>  handleTransactionNotFoundException(TransactionNotFoundException ex) {
        logger.warn("Transaction not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.error(404, "Transaction not found"));
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<CommonResponse<Map<String, String>>> handleDuplicateTransactionException(DuplicateTransactionException ex) {
        logger.warn("Duplicate transaction attempt: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(CommonResponse.error(409, "Duplicate transaction"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(CommonResponse.error(400, ErrorMessages.INVALID_INPUT_DATA));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Map<String, String>>> handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error(500, "Internal server error"));
    }
}