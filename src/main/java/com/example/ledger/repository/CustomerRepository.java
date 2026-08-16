package com.example.ledger.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.ledger.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer,Long>,JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByIdAndUser_Id(Long id,Long userId);

    long countByUser_Id(Long userId);
}
