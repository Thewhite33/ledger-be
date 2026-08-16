package com.example.ledger.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ledger.entity.Transaction;
import com.example.ledger.entity.TransactionStatus;

import jakarta.persistence.LockModeType;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    @EntityGraph(attributePaths = { "customer" })
    Optional<Transaction> findByIdAndCustomer_User_Id(Long id, Long userId);

    @EntityGraph(attributePaths = { "customer" })
    @Override
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    @EntityGraph(attributePaths = { "customer" })
    List<Transaction> findTop5ByCustomer_User_IdOrderByCreatedAtDesc(Long userId);

    boolean existsByCustomer_Id(Long customerId);

    long countByCustomer_User_Id(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select t
            from Transaction t
            where t.id = :id
            and t.customer.user.id = :userId
            """)
    Optional<Transaction> findLockedByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Transaction t
            set t.status = :overdueStatus
            where t.customer.user.id = :userId
            and t.status in :statuses
            and t.dueDate is not null
            and t.dueDate < :today
            """)
    int markOverdueForUser(
            @Param("userId") Long userId,
            @Param("overdueStatus") TransactionStatus overdueStatus,
            @Param("statuses") List<TransactionStatus> statuses,
            @Param("today") LocalDate today);

    @Query(
            value = """
                    SELECT COALESCE(SUM(t.amount - COALESCE(p.paid, 0)), 0)
                    FROM transactions t
                    LEFT JOIN (
                        SELECT transaction_id, SUM(amount) AS paid
                        FROM payments
                        GROUP BY transaction_id
                    ) p ON p.transaction_id = t.id
                    JOIN customers c ON c.id = t.customer_id
                    WHERE c.user_id = :userId
                    AND t.status <> 'PAID'
                    """,
            nativeQuery = true
    )
    BigDecimal totalPendingAmount(@Param("userId") Long userId);

    @Query(
            value = """
                    SELECT COALESCE(SUM(t.amount - COALESCE(p.paid, 0)), 0)
                    FROM transactions t
                    LEFT JOIN (
                        SELECT transaction_id, SUM(amount) AS paid
                        FROM payments
                        GROUP BY transaction_id
                    ) p ON p.transaction_id = t.id
                    JOIN customers c ON c.id = t.customer_id
                    WHERE c.user_id = :userId
                      AND t.status = 'OVERDUE'
                    """,
            nativeQuery = true
    )
    BigDecimal totalOverdueAmount(@Param("userId") Long userId);

    @Query(
            value = """
                    SELECT COALESCE(SUM(p.amount), 0)
                    FROM payments p
                    JOIN transactions t ON t.id = p.transaction_id
                    JOIN customers c ON c.id = t.customer_id
                    WHERE c.user_id = :userId
                    """,
            nativeQuery = true
    )
    BigDecimal totalCollectedAmount(@Param("userId") Long userId);
}
