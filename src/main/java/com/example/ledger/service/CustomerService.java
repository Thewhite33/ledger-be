package com.example.ledger.service;

import com.example.ledger.dto.customer.CustomerRequest;
import com.example.ledger.dto.customer.CustomerResponse;
import com.example.ledger.entity.Customer;
import com.example.ledger.entity.User;
import com.example.ledger.exception.ResourceConflictException;
import com.example.ledger.exception.ResourceNotFoundException;
import com.example.ledger.repository.CustomerRepository;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.repository.UserRepository;
import com.example.ledger.security.CurrentUserService;
import com.example.ledger.specification.CustomerSpecifications;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setName(request.getName().trim());
        customer.setPhone(normalizePhone(request.getPhone()));

        Customer savedCustomer = customerRepository.save(customer);

        return toResponse(savedCustomer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAll(String search, Pageable pageable) {
        Long userId = currentUserService.getCurrentUserId();

        Specification<Customer> specification =
                CustomerSpecifications.filter(userId, search);

        return customerRepository.findAll(specification, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        Customer customer = getOwnedCustomer(id);
        return toResponse(customer);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getOwnedCustomer(id);

        customer.setName(request.getName().trim());
        customer.setPhone(normalizePhone(request.getPhone()));

        Customer savedCustomer = customerRepository.save(customer);

        return toResponse(savedCustomer);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = getOwnedCustomer(id);

        boolean hasTransactions = transactionRepository.existsByCustomer_Id(customer.getId());

        if (hasTransactions) {
            throw new ResourceConflictException(
                    "Customer has transactions. Delete related transactions first."
            );
        }

        customerRepository.delete(customer);
    }

    private Customer getOwnedCustomer(Long customerId) {
        Long userId = currentUserService.getCurrentUserId();

        return customerRepository.findByIdAndUser_Id(customerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }

        String trimmed = phone.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}