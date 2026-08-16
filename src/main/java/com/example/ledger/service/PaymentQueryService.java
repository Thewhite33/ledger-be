package com.example.ledger.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ledger.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {
    private final PaymentRepository paymentRepository;

    public BigDecimal sumForTransaction(Long transactionId){
        BigDecimal sum = paymentRepository.sumByTransactionId(transactionId);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    public Map<Long,BigDecimal> sumsForTransactions(List<Long> transactionIds){
        Map<Long,BigDecimal> result = new HashMap<>();

        if(transactionIds == null || transactionIds.isEmpty()){
            return result;
        }

        List<Object[]> rows = paymentRepository.sumByTransactionIds(transactionIds);

        for(Object[] row : rows){
            Long transactionId = ((Number) row[0]).longValue();

            Object amountValue = row[1];
            BigDecimal amount;

            if (amountValue == null) {
                amount = BigDecimal.ZERO;
            } else if (amountValue instanceof BigDecimal bigDecimal) {
                amount = bigDecimal;
            } else {
                amount = new BigDecimal(amountValue.toString());
            }

            result.put(transactionId, amount);
        }
        return result;
    }
}
