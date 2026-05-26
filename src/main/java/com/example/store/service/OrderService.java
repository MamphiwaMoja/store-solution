package com.example.store.service;

import com.example.store.dto.request.CreateOrderRequest;
import com.example.store.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<Order> getAllOrders(Pageable pageable);

    Order getOrder(Long id);

    Order createOrder(CreateOrderRequest request);
}
