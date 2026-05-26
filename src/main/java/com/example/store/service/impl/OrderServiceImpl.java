package com.example.store.service.impl;

import com.example.store.dto.request.CreateOrderRequest;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.service.OrderService;
import com.example.store.web.rest.errors.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        Page<Long> idPage = orderRepository.findPageIds(pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, Order> ordersById = orderRepository.findAllWithDetailsByIdIn(idPage.getContent()).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));
        List<Order> orderedContent =
                idPage.getContent().stream().map(ordersById::get).toList();
        return new PageImpl<>(orderedContent, pageable, idPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository
                .findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order %d was not found".formatted(id)));
    }

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository
                .findById(request.customerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer %d was not found".formatted(request.customerId())));

        List<Product> products = productRepository.findAllById(request.productIds());
        Set<Long> requestedProductIds = new LinkedHashSet<>(request.productIds());
        if (products.size() != requestedProductIds.size()) {
            Set<Long> foundProductIds = products.stream()
                    .map(Product::getId)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            requestedProductIds.removeAll(foundProductIds);
            throw new ResourceNotFoundException("Product IDs were not found: " + requestedProductIds);
        }

        Order order = new Order();
        order.setDescription(request.description().trim());
        order.setCustomer(customer);
        order.setProducts(new LinkedHashSet<>(products));

        return orderRepository.save(order);
    }
}
