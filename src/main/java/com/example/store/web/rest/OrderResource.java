package com.example.store.web.rest;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.request.CreateOrderRequest;
import com.example.store.dto.response.PageResponse;
import com.example.store.mapper.OrderMapper;
import com.example.store.service.OrderService;

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
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderResource {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping
    public ResponseEntity<PageResponse<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        log.debug("REST request to list orders page={} size={}", page, size);
        return ResponseEntity.ok(PageResponse.from(orderService
                .getAllOrders(PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(orderMapper::orderToOrderDTO)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(
            @PathVariable @Positive(message = "id must be greater than zero") Long id) {
        log.debug("REST request to get order id={}", id);
        return ResponseEntity.ok(orderMapper.orderToOrderDTO(orderService.getOrder(id)));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.debug("REST request to create order for customerId={}", request.customerId());
        OrderDTO created = orderMapper.orderToOrderDTO(orderService.createOrder(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
