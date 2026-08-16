package com.example.ledger.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ledger.dto.dashboard.DashboardSummaryResponse;
import com.example.ledger.dto.transaction.TransactionResponse;
import com.example.ledger.entity.Transaction;
import com.example.ledger.entity.TransactionStatus;
import com.example.ledger.repository.CustomerRepository;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.security.CurrentUserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentQueryService paymentQueryService;
    private final TransactionMapper transactionMapper;
    private final CurrentUserService currentUserService;

    @Transactional
    public DashboardSummaryResponse getSummary() {
        Long userId = currentUserService.getCurrentUserId();

        transactionRepository.markOverdueForUser(
                userId,
                TransactionStatus.OVERDUE,
                List.of(TransactionStatus.PENDING, TransactionStatus.PARTIALLY_PAID),
                LocalDate.now()
        );

        long totalCustomers = customerRepository.countByUser_Id(userId);
        BigDecimal totalPendingAmount = transactionRepository.totalPendingAmount(userId);
        BigDecimal totalCollectedAmount = transactionRepository.totalCollectedAmount(userId);
        BigDecimal totalOverdueAmount = transactionRepository.totalOverdueAmount(userId);

        List<Transaction> recentTransactions =
                transactionRepository.findTop5ByCustomer_User_IdOrderByCreatedAtDesc(userId);

        List<Long> recentTransactionIds = recentTransactions
                .stream()
                .map(Transaction::getId)
                .toList();

        Map<Long, BigDecimal> paidAmountByTransactionId =
                paymentQueryService.sumsForTransactions(recentTransactionIds);

        List<TransactionResponse> recentTransactionResponses = recentTransactions
                .stream()
                .map(transaction ->
                        transactionMapper.toResponse(
                                transaction,
                                paidAmountByTransactionId.getOrDefault(transaction.getId(), BigDecimal.ZERO)
                        )
                )
                .toList();

        return DashboardSummaryResponse.builder()
                .totalCustomers(totalCustomers)
                .totalPendingAmount(totalPendingAmount)
                .totalCollectedAmount(totalCollectedAmount)
                .totalOverdueAmount(totalOverdueAmount)
                .recentTransactions(recentTransactionResponses)
                .build();
    }
}
