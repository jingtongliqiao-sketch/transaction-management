package com.hsbc.transactionmanagement.service;

import com.hsbc.transactionmanagement.entity.Transaction;
import com.hsbc.transactionmanagement.exception.DuplicateTransactionException;
import com.hsbc.transactionmanagement.exception.TransactionNotFoundException;
import com.hsbc.transactionmanagement.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CacheManager cacheManager;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, CacheManager cacheManager) {
        this.transactionRepository = transactionRepository;
        this.cacheManager = cacheManager;
    }

    @Caching(evict = {
            @CacheEvict(value = "transactions", allEntries = true),
            @CacheEvict(value = "transaction", key = "#transaction.id", condition = "#transaction.id != null")
    })
    public Transaction createTransaction(Transaction transaction) {
        if (transactionRepository.existsByTransactionReference(transaction.getTransactionReference())) {
            throw new DuplicateTransactionException(
                    "Transaction with reference " + transaction.getTransactionReference() + " already exists");
        }
        Transaction savedTransaction = transactionRepository.save(transaction);
        return savedTransaction;
    }

    @Cacheable(value = "transaction", key = "#id")
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Cacheable(value = "transaction", key = "'ref_' + #reference")
    public Transaction getTransactionByReference(String reference) {
        return transactionRepository.findByTransactionReference(reference).orElse(null);
    }

    @Cacheable(value = "transactions", key = "T(java.util.Objects).hash(#pageable.pageNumber, #pageable.pageSize, #category, #type)")
    public Page<Transaction> getAllTransactions(String category, String type, Pageable pageable) {
        return transactionRepository.findByFilters(category, type, pageable);
    }

    @Caching(evict = {
            @CacheEvict(value = "transactions", allEntries = true),
            @CacheEvict(value = "transaction", key = "#id")
    })
    public Transaction updateTransaction(Long id, Transaction transactionDetails) {
        Transaction transaction = getTransactionById(id);
        if (transaction == null) {
            throw new TransactionNotFoundException("Transaction not found with id: " + id);
        }

        transaction.setDescription(transactionDetails.getDescription());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setType(transactionDetails.getType());
        transaction.setCategory(transactionDetails.getCategory());

        return transactionRepository.save(transaction);
    }

    @Caching(evict = {
            @CacheEvict(value = "transactions", allEntries = true),
            @CacheEvict(value = "transaction", key = "#id")
    })
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new TransactionNotFoundException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    @Caching(evict = {
            @CacheEvict(value = "transactions", allEntries = true),
            @CacheEvict(value = "transaction", key = "'ref_' + #reference")
    })
    public void deleteTransactionByReference(String reference) {
        Transaction transaction = getTransactionByReference(reference);
        if (transaction == null) {
            throw new TransactionNotFoundException("Transaction not found with reference: " + reference);
        }
        transactionRepository.deleteById(transaction.getId());
    }

    public void clearCache() {
        for (String name : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}