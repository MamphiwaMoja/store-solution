package com.example.store.web.rest;

import com.example.store.dto.ProductDTO;
import com.example.store.dto.request.CreateProductRequest;
import com.example.store.dto.response.PageResponse;
import com.example.store.mapper.ProductMapper;
import com.example.store.service.ProductService;

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
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductResource {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<PageResponse<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        log.debug("REST request to list products page={} size={}", page, size);
        return ResponseEntity.ok(PageResponse.from(productService
                .getAllProducts(PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(productMapper::productToProductDTO)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(
            @PathVariable @Positive(message = "id must be greater than zero") Long id) {
        log.debug("REST request to get product id={}", id);
        return ResponseEntity.ok(productMapper.productToProductDTO(productService.getProduct(id)));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.debug("REST request to create product");
        ProductDTO created = productMapper.productToProductDTO(productService.createProduct(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
