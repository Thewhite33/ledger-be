package com.example.ledger.service;

import org.springframework.stereotype.Component;

import com.example.ledger.dto.transaction.TransactionResponse;
import com.example.ledger.entity.Transaction;
import com.example.ledger.entity.TransactionStatus;

import java.math.BigDecimal;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction, BigDecimal paidAmount) {
        BigDecimal paid = paidAmount == null ? BigDecimal.ZERO : paidAmount;

        BigDecimal remaining = transaction.getAmount().subtract(paid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        TransactionStatus status = StatusCalculator.calculate(
                transaction.getAmount(),
                paid,
                transaction.getDueDate()
        );

        return TransactionResponse.builder()
                .id(transaction.getId())
                .customerId(transaction.getCustomer().getId())
                .customerName(transaction.getCustomer().getName())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .dueDate(transaction.getDueDate())
                .status(status)
                .paidAmount(paid)
                .remainingAmount(remaining)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
