package com.hsbc.transactionmanagement.service;

import com.hsbc.transactionmanagement.entity.Transaction;
import com.hsbc.transactionmanagement.exception.DuplicateTransactionException;
import com.hsbc.transactionmanagement.exception.TransactionNotFoundException;
import com.hsbc.transactionmanagement.repository.TransactionRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = TestDataGenerator.createSampleTransaction();
        sampleTransaction.setId(1L);
    }

    @Test
    void createTransaction_ShouldReturnSavedTransaction_WhenValidInput() {
        when(transactionRepository.existsByTransactionReference(any())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        Transaction result = transactionService.createTransaction(sampleTransaction);

        assertNotNull(result);
        assertEquals(sampleTransaction.getId(), result.getId());
        verify(transactionRepository, times(1)).save(sampleTransaction);
    }

    @Test
    void createTransaction_ShouldThrowException_WhenDuplicateReference() {
        when(transactionRepository.existsByTransactionReference(any())).thenReturn(true);

        assertThrows(DuplicateTransactionException.class, () -> {
            transactionService.createTransaction(sampleTransaction);
        });
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenExists() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(sampleTransaction));

        Transaction result = transactionService.getTransactionById(1L);

        assertNotNull(result);
        assertEquals(sampleTransaction.getId(), result.getId());
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void getTransactionById_ShouldReturnNull_WhenNotFound() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        Transaction result = transactionService.getTransactionById(999L);

        assertNull(result);
        verify(transactionRepository, times(1)).findById(999L);
    }

    @Test
    void getTransactionByReference_ShouldReturnTransaction_WhenExists() {
        when(transactionRepository.findByTransactionReference("REF-123456")).thenReturn(Optional.of(sampleTransaction));

        Transaction result = transactionService.getTransactionByReference("REF-123456");

        assertNotNull(result);
        assertEquals(sampleTransaction.getId(), result.getId());
        verify(transactionRepository, times(1)).findByTransactionReference("REF-123456");
    }

    @Test
    void getTransactionByReference_ShouldReturnNull_WhenNotFound() {
        when(transactionRepository.findByTransactionReference("NONEXISTENT")).thenReturn(Optional.empty());

        Transaction result = transactionService.getTransactionByReference("NONEXISTENT");

        assertNull(result);
        verify(transactionRepository, times(1)).findByTransactionReference("NONEXISTENT");
    }

    @Test
    void getAllTransactions_ShouldReturnPaginatedResults() {
        List<Transaction> transactions = TestDataGenerator.createMultipleTransactions(5);
        Page<Transaction> transactionPage = new PageImpl<>(transactions);
        Pageable pageable = PageRequest.of(0, 10);

        when(transactionRepository.findByFilters(null, null, pageable)).thenReturn(transactionPage);

        Page<Transaction> result = transactionService.getAllTransactions(null, null, pageable);

        assertNotNull(result);
        assertEquals(5, result.getTotalElements());
        verify(transactionRepository, times(1)).findByFilters(null, null, pageable);
    }

    @Test
    void updateTransaction_ShouldUpdateExistingTransaction() {
        Transaction updatedDetails = TestDataGenerator.createSampleTransaction();
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setAmount(new BigDecimal("200.00"));

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        Transaction result = transactionService.updateTransaction(1L, updatedDetails);

        assertNotNull(result);
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void updateTransaction_ShouldThrowException_WhenNotFound() {
        Transaction updatedDetails = TestDataGenerator.createSampleTransaction();
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.updateTransaction(999L, updatedDetails);
        });
        verify(transactionRepository, times(1)).findById(999L);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deleteTransaction_ShouldDeleteExistingTransaction() {
        when(transactionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(transactionRepository).deleteById(1L);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository, times(1)).existsById(1L);
        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTransaction_ShouldThrowException_WhenNotFound() {
        when(transactionRepository.existsById(999L)).thenReturn(false);

        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.deleteTransaction(999L);
        });
        verify(transactionRepository, times(1)).existsById(999L);
        verify(transactionRepository, never()).deleteById(any());
    }

    @Test
    void deleteTransactionByReference_ShouldDeleteByReference() {
        when(transactionRepository.findByTransactionReference("REF-123456")).thenReturn(Optional.of(sampleTransaction));
        doNothing().when(transactionRepository).deleteById(1L);

        transactionService.deleteTransactionByReference("REF-123456");

        verify(transactionRepository, times(1)).findByTransactionReference("REF-123456");
        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTransactionByReference_ShouldThrowException_WhenNotFound() {
        when(transactionRepository.findByTransactionReference("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.deleteTransactionByReference("NONEXISTENT");
        });
        verify(transactionRepository, times(1)).findByTransactionReference("NONEXISTENT");
        verify(transactionRepository, never()).deleteById(any());
    }

    @Test
    void getAllTransactions_WithFilters_ShouldReturnFilteredResults() {
        List<Transaction> filteredTransactions = List.of(sampleTransaction);
        Page<Transaction> transactionPage = new PageImpl<>(filteredTransactions);
        Pageable pageable = PageRequest.of(0, 10);

        when(transactionRepository.findByFilters("Shopping", "DEBIT", pageable))
                .thenReturn(transactionPage);

        Page<Transaction> result = transactionService.getAllTransactions("Shopping", "DEBIT", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository, times(1)).findByFilters("Shopping", "DEBIT", pageable);
    }
}