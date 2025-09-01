package com.hsbc.transactionmanagement.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = @Index(name = "idx_txn_ref", columnList = "transactionReference"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction entity representing a financial transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the transaction", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    // create request will not use id from payload
    private Long id;

    @NotBlank(message = "Description is mandatory")
    @Size(max = 255, message = "Description must be less than 255 characters")
    @Schema(description = "Description of the transaction", example = "Grocery shopping", required = true)
    private String description;

    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Schema(description = "Transaction amount", example = "100.50", required = true)
    @Column(precision = 19, scale = 4)
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @NotBlank(message = "Type is mandatory")
    @Pattern(regexp = "^(DEBIT|CREDIT)$", message = "Type must be either DEBIT or CREDIT")
    @Schema(description = "Type of transaction", example = "DEBIT", allowableValues = {"DEBIT", "CREDIT"}, required = true)
    private String type;

    @NotBlank(message = "Category is mandatory")
    @Size(max = 100, message = "Category must be less than 100 characters")
    @Schema(description = "Category of the transaction", example = "Food", required = true)
    private String category;

    @Column(unique = true)
    @NotBlank(message = "Transaction reference is mandatory")
    @Size(max = 50, message = "Transaction reference must be less than 50 characters")
    @Schema(description = "Unique reference number for the transaction", example = "REF123456", required = true)
    private String transactionReference;

    @Schema(description = "Date and time when the transaction was created", example = "2023-08-29T15:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }
}