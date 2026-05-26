package com.example.store.web.rest;

import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.service.ProductService;
import com.example.store.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductResource.class)
@ComponentScan(basePackageClasses = ProductMapper.class)
@Import(ExceptionTranslator.class)
class ProductResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        Order order = new Order();
        order.setId(11L);
        order.setDescription("Order with product");
        order.setCustomer(customer);

        product = new Product();
        product.setId(7L);
        product.setDescription("Keyboard");
        product.setOrders(new LinkedHashSet<>(List.of(order)));
    }

    @Test
    void createProductReturnsCreatedProduct() throws Exception {
        when(productService.createProduct(any())).thenReturn(product);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Keyboard\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/products/7"))
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.description").value("Keyboard"))
                .andExpect(jsonPath("$.orderIds[0]").value(11L));
    }

    @Test
    void getProductsReturnsProductsWithOrderIds() throws Exception {
        when(productService.getAllProducts(any()))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 50), 1));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7L))
                .andExpect(jsonPath("$.content[0].orderIds[0]").value(11L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getProductByIdReturnsSpecificProduct() throws Exception {
        when(productService.getProduct(7L)).thenReturn(product);

        mockMvc.perform(get("/products/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Keyboard"))
                .andExpect(jsonPath("$.orderIds[0]").value(11L));
    }

    @Test
    void createProductRejectsBlankDescription() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.description").exists());
    }
}
