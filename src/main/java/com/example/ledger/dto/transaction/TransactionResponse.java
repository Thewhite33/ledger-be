package com.example.ledger.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.ledger.entity.TransactionStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private Long customerId;
    private String customerName;

    private BigDecimal amount;
    private String description;

    private LocalDate transactionDate;
    private LocalDate dueDate;

    private TransactionStatus status;

    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    private LocalDateTime createdAt;
}
