package com.example.ledger.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

import com.example.ledger.dto.transaction.TransactionResponse;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalCustomers;
    private BigDecimal totalPendingAmount;
    private BigDecimal totalCollectedAmount;
    private BigDecimal totalOverdueAmount;
    private List<TransactionResponse> recentTransactions;

}