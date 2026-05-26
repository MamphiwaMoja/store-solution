package com.example.store.web.rest;

import com.example.store.dto.CustomerDTO;
import com.example.store.dto.request.CreateCustomerRequest;
import com.example.store.dto.response.PageResponse;
import com.example.store.mapper.CustomerMapper;
import com.example.store.service.CustomerService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@Validated
@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerResource {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @GetMapping
    public ResponseEntity<PageResponse<CustomerDTO>> getCustomers(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        log.debug("REST request to list customers with query={} page={} size={}", query, page, size);
        return ResponseEntity.ok(PageResponse.from(customerService
                .getCustomers(query, PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(customerMapper::customerToCustomerDTO)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(
            @PathVariable @Positive(message = "id must be greater than zero") Long id) {
        log.debug("REST request to get customer id={}", id);
        return ResponseEntity.ok(customerMapper.customerToCustomerDTO(customerService.getCustomer(id)));
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        log.debug("REST request to create customer");
        CustomerDTO created = customerMapper.customerToCustomerDTO(customerService.createCustomer(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
