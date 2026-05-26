package com.example.store.service.impl;

import com.example.store.dto.request.CreateProductRequest;
import com.example.store.entity.Product;
import com.example.store.repository.ProductRepository;
import com.example.store.service.ProductService;
import com.example.store.web.rest.errors.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        Page<Long> idPage = productRepository.findPageIds(pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, Product> productsById = productRepository.findAllWithOrdersByIdIn(idPage.getContent()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Product> orderedContent =
                idPage.getContent().stream().map(productsById::get).toList();
        return new PageImpl<>(orderedContent, pageable, idPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository
                .findByIdWithOrders(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product %d was not found".formatted(id)));
    }

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setDescription(request.description().trim());
        return productRepository.save(product);
    }
}
