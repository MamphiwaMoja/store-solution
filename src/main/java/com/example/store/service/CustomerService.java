package com.example.store.service;

import com.example.store.dto.request.CreateCustomerRequest;
import com.example.store.entity.Customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Page<Customer> getCustomers(String query, Pageable pageable);

    Customer getCustomer(Long id);

    Customer createCustomer(CreateCustomerRequest request);
}
