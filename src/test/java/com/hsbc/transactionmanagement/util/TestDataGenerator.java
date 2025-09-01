package com.hsbc.transactionmanagement.util;

import com.hsbc.transactionmanagement.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestDataGenerator {

    public static Transaction createSampleTransaction() {
        return createTransaction( "Test Transaction", new BigDecimal("100.00"), "DEBIT", "Shopping");
    }

    public static Transaction createTransaction(String description, BigDecimal amount, String type, String category) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setTransactionReference(generateReference());
        return transaction;
    }

    public static String generateReference() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static List<Transaction> createMultipleTransactions(int count) {
        List<Transaction> transactions = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            transactions.add(createTransaction(
                    "Transaction " + (i + 1),
                    new BigDecimal(10 + i * 5),
                    i % 2 == 0 ? "DEBIT" : "CREDIT",
                    i % 3 == 0 ? "Shopping" : i % 3 == 1 ? "Food" : "Transport"
            ));
        }
        return transactions;
    }

    public static Transaction createInvalidTransaction() {
        Transaction transaction = new Transaction();
        transaction.setDescription(""); // 空描述
        transaction.setAmount(BigDecimal.ZERO); // 金额为0
        transaction.setType("INVALID"); // 无效类型
        transaction.setCategory(""); // 空类别
        transaction.setTransactionReference(""); // 空引用
        return transaction;
    }
}