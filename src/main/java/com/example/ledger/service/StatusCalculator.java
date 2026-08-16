package com.example.ledger.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.ledger.entity.TransactionStatus;

public final class StatusCalculator {

    private StatusCalculator() {
    }

    public static TransactionStatus calculate(
            BigDecimal amount,
            BigDecimal paidAmount,
            LocalDate dueDate) {
        BigDecimal paid = paidAmount == null ? BigDecimal.ZERO : paidAmount;

        if (paid.compareTo(amount) >= 0) {
            return TransactionStatus.PAID;
        }

        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            return TransactionStatus.OVERDUE;
        }

        if (paid.compareTo(BigDecimal.ZERO) > 0) {
            return TransactionStatus.PARTIALLY_PAID;
        }

        return TransactionStatus.PENDING;
    }
}