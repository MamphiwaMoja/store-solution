package com.example.store.service;

import com.example.store.dto.request.CreateProductRequest;
import com.example.store.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<Product> getAllProducts(Pageable pageable);

    Product getProduct(Long id);

    Product createProduct(CreateProductRequest request);
}
