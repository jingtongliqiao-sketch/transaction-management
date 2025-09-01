package com.hsbc.transactionmanagement.repository;

import com.hsbc.transactionmanagement.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    boolean existsByTransactionReference(String transactionReference);

    boolean existsById(Long id);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:category IS NULL OR LOWER(t.category) = LOWER(:category)) AND " +
            "(:type IS NULL OR LOWER(t.type) = LOWER(:type))")
    Page<Transaction> findByFilters(
            @Param("category") String category,
            @Param("type") String type,
            Pageable pageable);

    void deleteByTransactionReference(String transactionReference);
}