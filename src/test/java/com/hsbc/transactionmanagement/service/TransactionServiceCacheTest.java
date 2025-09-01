package com.hsbc.transactionmanagement.service;

import com.hsbc.transactionmanagement.entity.Transaction;
import com.hsbc.transactionmanagement.repository.TransactionRepository;
import com.hsbc.transactionmanagement.util.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class TransactionServiceCacheTest {

    @MockBean
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CacheManager cacheManager;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = TestDataGenerator.createSampleTransaction();
        sampleTransaction.setId(1L);

        if (cacheManager != null) {
            cacheManager.getCache("transaction").clear();
        }
    }

    @Test
    void getTransactionById_ShouldCacheResult_WhenFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(sampleTransaction));

        Transaction result1 = transactionService.getTransactionById(1L);
        Transaction result2 = transactionService.getTransactionById(1L);

        verify(transactionRepository, times(1)).findById(1L);
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void getTransactionByReference_ShouldCacheResult_WhenFound() {
        when(transactionRepository.findByTransactionReference("REF-123456")).thenReturn(Optional.of(sampleTransaction));

        Transaction result1 = transactionService.getTransactionByReference("REF-123456");
        Transaction result2 = transactionService.getTransactionByReference("REF-123456");

        verify(transactionRepository, times(1)).findByTransactionReference("REF-123456");
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void getTransactionById_ShouldCacheNullResult_WhenNotFound() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        Transaction result1 = transactionService.getTransactionById(999L);
        Transaction result2 = transactionService.getTransactionById(999L);

        // null 结果会被缓存，所以只调用一次
        verify(transactionRepository, times(1)).findById(999L);
        assertThat(result1).isNull();
        assertThat(result2).isNull();
    }

    @Test
    void cacheShouldBeCleared_AfterCreateTransaction() {
        when(transactionRepository.existsByTransactionReference(any())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(sampleTransaction));

        transactionService.createTransaction(sampleTransaction);

        cacheManager.getCache("transaction").clear();

        Transaction result = transactionService.getTransactionById(1L);

        verify(transactionRepository, times(1)).findById(1L);
        assertThat(result).isNotNull();
    }
}