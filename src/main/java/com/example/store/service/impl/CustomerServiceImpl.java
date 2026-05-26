package com.example.store.service.impl;

import com.example.store.dto.request.CreateCustomerRequest;
import com.example.store.entity.Customer;
import com.example.store.repository.CustomerRepository;
import com.example.store.service.CustomerService;
import com.example.store.web.rest.errors.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> getCustomers(String query, Pageable pageable) {
        Page<Long> idPage = StringUtils.hasText(query)
                ? customerRepository.searchPageIdsByNameContainingIgnoreCase(query.trim(), pageable)
                : customerRepository.findPageIds(pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, Customer> customersById = customerRepository.findAllWithOrdersByIdIn(idPage.getContent()).stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));
        List<Customer> orderedContent =
                idPage.getContent().stream().map(customersById::get).toList();
        return new PageImpl<>(orderedContent, pageable, idPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomer(Long id) {
        return customerRepository
                .findByIdWithOrders(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer %d was not found".formatted(id)));
    }

    @Override
    @Transactional
    public Customer createCustomer(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.name().trim());
        return customerRepository.save(customer);
    }
}
