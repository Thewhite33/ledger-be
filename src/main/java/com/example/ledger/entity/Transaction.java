package com.example.ledger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions", indexes = {
                @Index(name = "idx_transactions_customer_id", columnList = "customer_id"),
                @Index(name = "idx_transactions_due_date", columnList = "due_date"),
                @Index(name = "idx_transactions_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "customer_id", nullable = false)
        private Customer customer;

        @Column(nullable = false, precision = 15, scale = 2)
        private BigDecimal amount;

        @Column(length = 255)
        private String description;

        @Column(name = "transaction_date", nullable = false)
        private LocalDate transactionDate;

        @Column(name = "due_date")
        private LocalDate dueDate;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private TransactionStatus status;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Payment> payments = new ArrayList<>();
}