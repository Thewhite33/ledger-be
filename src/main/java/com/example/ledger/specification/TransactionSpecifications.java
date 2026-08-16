package com.example.ledger.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.example.ledger.entity.Transaction;
import com.example.ledger.entity.TransactionStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> filter(
            Long userId,
            Long customerId,
            TransactionStatus status,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    criteriaBuilder.equal(
                            root.get("customer").get("user").get("id"),
                            userId
                    )
            );

            if (customerId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("customer").get("id"),
                                customerId
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            if (fromDate != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("transactionDate"),
                                fromDate
                        )
                );
            }

            if (toDate != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("transactionDate"),
                                toDate
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
