package com.hsbc.transactionmanagement.integration;

import com.hsbc.transactionmanagement.entity.Transaction;
import com.hsbc.transactionmanagement.repository.TransactionRepository;
import com.hsbc.transactionmanagement.util.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    private Transaction savedTransaction;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        savedTransaction = TestDataGenerator.createSampleTransaction();
        savedTransaction = transactionRepository.save(savedTransaction);
    }

    @Test
    void createTransaction_ShouldPersistTransaction() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")  // 更新路径
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "description": "New Transaction",
                        "amount": 50.00,
                        "type": "CREDIT",
                        "category": "Salary",
                        "transactionReference": "REF-NEW123"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.description").value("New Transaction"));
    }

    @Test
    void getAllTransactions_ShouldReturnSavedTransactions() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")  // 更新路径
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.content", hasSize(1)))
                .andExpect(jsonPath("$.result.content[0].description").value("Test Transaction"));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/{id}", savedTransaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.id").value(savedTransaction.getId()))
                .andExpect(jsonPath("$.result.description").value("Test Transaction"));
    }

    @Test
    void getTransactionById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void updateTransaction_ShouldUpdateExistingTransaction() throws Exception {
        mockMvc.perform(put("/api/v1/transactions/{id}", savedTransaction.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "description": "Updated Transaction",
                                "amount": 200.00,
                                "type": "DEBIT",
                                "category": "Updated Category",
                                "transactionReference": "REF123456"
                            }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.description").value("Updated Transaction"));
    }

    @Test
    void updateTransaction_ShouldReturnNotFound_WhenNotExists() throws Exception {
        mockMvc.perform(put("/api/v1/transactions/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "description": "Duplicate Transaction",
                                "amount": 100.00,
                                "type": "DEBIT",
                                "category": "Shopping",
                                "transactionReference": "AAAA"
                            }
                    """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void deleteTransaction_ShouldRemoveTransaction() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/{id}", savedTransaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200));

        // Verify transaction is actually deleted
        mockMvc.perform(get("/api/v1/transactions/{id}", savedTransaction.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTransaction_ShouldReturnNotFound_WhenNotExists() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void createTransaction_ShouldReturnError_WhenDuplicateReference() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")  // 更新路径
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                    {
                        "description": "Duplicate Transaction",
                        "amount": 100.00,
                        "type": "DEBIT",
                        "category": "Shopping",
                        "transactionReference": "%s"
                    }
                    """, savedTransaction.getTransactionReference())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status.code").value(409));
    }

    @Test
    void getTransactions_WithFilters_ShouldReturnFilteredResults() throws Exception {
        // Add more test data
        Transaction foodTransaction = TestDataGenerator.createTransaction(
                "Food Purchase", new BigDecimal("25.00"), "DEBIT", "Food"
        );
        transactionRepository.save(foodTransaction);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("category", "Food")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content", hasSize(1)))
                .andExpect(jsonPath("$.result.content[0].category").value("Food"));
    }

    @Test
    void getTransactionByReference_ShouldReturnTransaction() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/reference/{reference}", savedTransaction.getTransactionReference()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.result.id").value(savedTransaction.getId()));
    }

    @Test
    void getTransactionByReference_ShouldReturnNotFound_WhenNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/reference/NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }

    @Test
    void deleteTransactionByReference_ShouldRemoveTransaction() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/reference/{reference}", savedTransaction.getTransactionReference()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200));

        // Verify transaction is actually deleted
        mockMvc.perform(get("/api/v1/transactions/reference/{reference}", savedTransaction.getTransactionReference()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTransactionByReference_ShouldReturnNotFound_WhenNotExists() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/reference/NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.code").value(404));
    }
}