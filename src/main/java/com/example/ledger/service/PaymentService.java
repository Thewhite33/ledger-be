package com.example.ledger.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ledger.dto.payment.PaymentRequest;
import com.example.ledger.dto.payment.PaymentResponse;
import com.example.ledger.entity.Payment;
import com.example.ledger.entity.Transaction;
import com.example.ledger.exception.InvalidPaymentException;
import com.example.ledger.exception.ResourceNotFoundException;
import com.example.ledger.repository.PaymentRepository;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.security.CurrentUserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentQueryService paymentQueryService;
    private final CurrentUserService currentUserService;

    @Transactional
    public PaymentResponse create(Long transactionId, PaymentRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        Transaction transaction = transactionRepository.findLockedByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        BigDecimal paidAmount = paymentQueryService.sumForTransaction(transaction.getId());
        BigDecimal remainingAmount = transaction.getAmount().subtract(paidAmount);

        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new InvalidPaymentException(
                    "Payment amount cannot exceed remaining amount"
            );
        }

        Payment payment = new Payment();
        payment.setTransaction(transaction);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setNote(trimToNull(request.getNote()));

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal newPaidAmount = paidAmount.add(savedPayment.getAmount());

        transaction.setStatus(
                StatusCalculator.calculate(
                        transaction.getAmount(),
                        newPaidAmount,
                        transaction.getDueDate()
                )
        );

        transactionRepository.save(transaction);

        return toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll(Long transactionId) {
        Long userId = currentUserService.getCurrentUserId();

        boolean transactionExists =
                transactionRepository.findByIdAndCustomer_User_Id(transactionId, userId).isPresent();

        if (!transactionExists) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        return paymentRepository.findByTransaction_IdOrderByPaymentDateDesc(transactionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .transactionId(payment.getTransaction().getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .note(payment.getNote())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}