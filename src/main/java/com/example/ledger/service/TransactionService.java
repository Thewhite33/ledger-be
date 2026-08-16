package com.example.ledger.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ledger.dto.transaction.TransactionRequest;
import com.example.ledger.dto.transaction.TransactionResponse;
import com.example.ledger.dto.transaction.TransactionUpdateRequest;
import com.example.ledger.entity.Customer;
import com.example.ledger.entity.Transaction;
import com.example.ledger.entity.TransactionStatus;
import com.example.ledger.exception.InvalidPaymentException;
import com.example.ledger.exception.ResourceNotFoundException;
import com.example.ledger.repository.CustomerRepository;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.security.CurrentUserService;
import com.example.ledger.specification.TransactionSpecifications;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final PaymentQueryService paymentQueryService;
    private final CurrentUserService currentUserService;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        Customer customer = customerRepository.findByIdAndUser_Id(request.getCustomerId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Transaction  transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(trimToNull(request.getDescription()));
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDueDate(request.getDueDate());
        transaction.setStatus(
                StatusCalculator.calculate(
                        request.getAmount(),
                        BigDecimal.ZERO,
                        request.getDueDate()
                )
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction, BigDecimal.ZERO);
    }

    @Transactional
    public Page<TransactionResponse> getAll(
            Long customerId,
            TransactionStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        Long userId = currentUserService.getCurrentUserId();

        if (status != null) {
            markOverdueTransactionsForUser(userId);
        }

        Specification<Transaction> specification = TransactionSpecifications.filter(
                userId,
                customerId,
                status,
                fromDate,
                toDate
        );

        Page<Transaction> transactionPage = transactionRepository.findAll(specification, pageable);

        List<Long> transactionIds = transactionPage.getContent()
                .stream()
                .map(Transaction::getId)
                .toList();

        Map<Long, BigDecimal> paidAmountByTransactionId =
                paymentQueryService.sumsForTransactions(transactionIds);

        List<TransactionResponse> responses = transactionPage.getContent()
                .stream()
                .map(transaction ->
                        transactionMapper.toResponse(
                                transaction,
                                paidAmountByTransactionId.getOrDefault(transaction.getId(), BigDecimal.ZERO)
                        )
                )
                .toList();

        return new PageImpl<>(responses, pageable, transactionPage.getTotalElements());
    }

    @Transactional
    public TransactionResponse getById(Long id) {
        Long userId = currentUserService.getCurrentUserId();

        Transaction transaction = getOwnedTransaction(id, userId);

        BigDecimal paidAmount = paymentQueryService.sumForTransaction(transaction.getId());

        TransactionStatus currentStatus = StatusCalculator.calculate(
                transaction.getAmount(),
                paidAmount,
                transaction.getDueDate()
        );

        if (transaction.getStatus() != currentStatus) {
            transaction.setStatus(currentStatus);
            transactionRepository.save(transaction);
        }

        return transactionMapper.toResponse(transaction, paidAmount);
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionUpdateRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        Transaction transaction = getOwnedTransaction(id, userId);

        BigDecimal paidAmount = paymentQueryService.sumForTransaction(transaction.getId());

        if (request.getAmount().compareTo(paidAmount) < 0) {
            throw new InvalidPaymentException(
                    "Transaction amount cannot be less than the amount already paid"
            );
        }

        transaction.setAmount(request.getAmount());
        transaction.setDescription(trimToNull(request.getDescription()));
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDueDate(request.getDueDate());
        transaction.setStatus(
                StatusCalculator.calculate(
                        request.getAmount(),
                        paidAmount,
                        request.getDueDate()
                )
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction, paidAmount);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUserId();

        Transaction transaction = getOwnedTransaction(id, userId);

        transactionRepository.delete(transaction);
    }

    private Transaction getOwnedTransaction(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndCustomer_User_Id(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private void markOverdueTransactionsForUser(Long userId) {
        transactionRepository.markOverdueForUser(
                userId,
                TransactionStatus.OVERDUE,
                List.of(TransactionStatus.PENDING, TransactionStatus.PARTIALLY_PAID),
                LocalDate.now()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}
