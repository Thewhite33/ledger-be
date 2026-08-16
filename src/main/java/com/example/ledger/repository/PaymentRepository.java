package com.example.ledger.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ledger.entity.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByTransaction_IdOrderByPaymentDateDesc(Long transactionId);

    @Query("""
            select coalesce(sum(p.amount), 0)
            from Payment p
            where p.transaction.id = :transactionId
            """)
    BigDecimal sumByTransactionId(@Param("transactionId") Long transactionId);

    @Query("""
            select p.transaction.id, sum(p.amount)
            from Payment p
            where p.transaction.id in :ids
            group by p.transaction.id
            """)
    List<Object[]> sumByTransactionIds(@Param("ids") List<Long> ids);
}