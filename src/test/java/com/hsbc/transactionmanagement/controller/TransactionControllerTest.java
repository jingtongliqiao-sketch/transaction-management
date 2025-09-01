package com.hsbc.transactionmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transactionmanagement.entity.Transaction;
import com.hsbc.transactionmanagement.exception.DuplicateTransactionException;
import com.hsbc.transactionmanagement.exception.TransactionNotFoundException;
import com.hsbc.transactionmanagement.handler.GlobalExceptionHandler;
import com.hsbc.transactionmanagement.service.TransactionService;
import com.hsbc.transactionmanagement.util.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        sampleTransaction = TestDataGenerator.createSampleTransaction();
        sampleTransaction.setId(1L);
    }

    @Test
    void createTransaction_ShouldReturnCreatedResponse_WhenValidInput() throws Exception {
        // Arrange
        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(sampleTransaction);

        // Act & Assert using MockMvc - 使用正确的路径 /api/v1/transactions
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.id").value(1L));
    }

    @Test
    void createTransaction_ShouldReturnConflict_WhenDuplicateReference() throws Exception {
        // Arrange
        when(transactionService.createTransaction(any(Transaction.class)))
                .thenThrow(new DuplicateTransactionException("Duplicate transaction"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status.code").value(409));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenExists() throws Exception {
        // Arrange
        when(transactionService.getTransactionById(1L)).thenReturn(sampleTransaction);

        // Act & Assert using MockMvc
        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.id").value(1L));
    }

    @Test
    void getTransactionById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        when(transactionService.getTransactionById(999L)).thenReturn(null);

        // Act & Assert using MockMvc
        mockMvc.perform(get("/api/v1/transactions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void getTransactionByReference_ShouldReturnTransaction_WhenExists() throws Exception {
        // Arrange
        when(transactionService.getTransactionByReference("REF-123456")).thenReturn(sampleTransaction);

        // Act & Assert using MockMvc
        mockMvc.perform(get("/api/v1/transactions/reference/REF-123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.id").value(1L));
    }

    @Test
    void getTransactionByReference_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        when(transactionService.getTransactionByReference("NONEXISTENT")).thenReturn(null);

        // Act & Assert using MockMvc
        mockMvc.perform(get("/api/v1/transactions/reference/NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void getAllTransactions_ShouldReturnPaginatedResponse() throws Exception {
        // Arrange
        List<Transaction> transactions = TestDataGenerator.createMultipleTransactions(3);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10), 3);

        when(transactionService.getAllTransactions(eq(null), eq(null), any(Pageable.class))).thenReturn(transactionPage);

        // Act & Assert using MockMvc
        mockMvc.perform(get("/api/v1/transactions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "transactionDate")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.totalItems").value(3));
    }

    @Test
    void getAllTransactions_WithFilters_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        List<Transaction> transactions = TestDataGenerator.createMultipleTransactions(2);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10), 2);

        when(transactionService.getAllTransactions(eq("Shopping"), eq("DEBIT"), any(Pageable.class)))
                .thenReturn(transactionPage);

        // Act & Assert using MockMvc
        mockMvc.perform(get("/api/v1/transactions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "transactionDate")
                        .param("direction", "desc")
                        .param("category", "Shopping")
                        .param("type", "DEBIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.totalItems").value(2));
    }

    @Test
    void updateTransaction_ShouldReturnUpdatedTransaction_WhenExists() throws Exception {
        // Arrange
        Transaction updatedTransaction = TestDataGenerator.createSampleTransaction();
        updatedTransaction.setDescription("Updated Description");

        when(transactionService.updateTransaction(eq(1L), any(Transaction.class))).thenReturn(updatedTransaction);

        // Act & Assert using MockMvc
        mockMvc.perform(put("/api/v1/transactions/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.description").value("Updated Description"));
    }

    @Test
    void updateTransaction_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        Transaction updatedTransaction = TestDataGenerator.createSampleTransaction();
        when(transactionService.updateTransaction(eq(999L), any(Transaction.class)))
                .thenThrow(new TransactionNotFoundException("Transaction not found"));

        // Act & Assert using MockMvc
        mockMvc.perform(put("/api/v1/transactions/999")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedTransaction)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void deleteTransaction_ShouldReturnSuccess_WhenExists() throws Exception {
        // Arrange
        doNothing().when(transactionService).deleteTransaction(1L);

        // Act & Assert using MockMvc
        mockMvc.perform(delete("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200));
    }

    @Test
    void deleteTransaction_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        doThrow(new TransactionNotFoundException("Transaction not found"))
                .when(transactionService).deleteTransaction(999L);

        // Act & Assert using MockMvc
        mockMvc.perform(delete("/api/v1/transactions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void deleteTransactionByReference_ShouldReturnSuccess_WhenExists() throws Exception {
        // Arrange
        doNothing().when(transactionService).deleteTransactionByReference("REF-123456");

        // Act & Assert using MockMvc
        mockMvc.perform(delete("/api/v1/transactions/reference/REF-123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200));
    }

    @Test
    void deleteTransactionByReference_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        doThrow(new TransactionNotFoundException("Transaction not found"))
                .when(transactionService).deleteTransactionByReference("NONEXISTENT");

        // Act & Assert using MockMvc
        mockMvc.perform(delete("/api/v1/transactions/reference/NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void createTransaction_ShouldHandleValidationErrors() throws Exception {
        // Arrange
        String invalidTransactionJson = """
        {
            "description": "",
            "amount": 0,
            "type": "INVALID_TYPE",
            "category": "",
            "transactionReference": ""
        }
        """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType("application/json")
                        .content(invalidTransactionJson))
                .andExpect(status().isBadRequest());
    }
}