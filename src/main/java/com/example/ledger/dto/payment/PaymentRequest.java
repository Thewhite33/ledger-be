package com.example.ledger.dto.payment;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Amount supports up to 2 decimal places")
    private BigDecimal amount;

    private LocalDate paymentDate;

    @Size(max = 255, message = "Note must be at most 255 characters")
    private String note;
}