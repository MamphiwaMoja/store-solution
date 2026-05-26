package com.example.store.web.rest;

import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.mapper.OrderMapper;
import com.example.store.service.OrderService;
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

@WebMvcTest(OrderResource.class)
@ComponentScan(basePackageClasses = OrderMapper.class)
@Import(ExceptionTranslator.class)
class OrderResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setId(1L);

        Product product = new Product();
        product.setId(7L);
        product.setDescription("Keyboard");

        order = new Order();
        order.setDescription("Test Order");
        order.setId(1L);
        order.setCustomer(customer);
        order.setProducts(new LinkedHashSet<>(List.of(product)));
    }

    @Test
    void createOrderReturnsCreatedOrder() throws Exception {
        when(orderService.createOrder(any())).thenReturn(order);

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Test Order\",\"customerId\":1,\"productIds\":[7]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/order/1"))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.name").value("John Doe"))
                .andExpect(jsonPath("$.products[0].id").value(7L))
                .andExpect(jsonPath("$.products[0].description").value("Keyboard"));
    }

    @Test
    void getAllOrdersReturnsOrdersWithProducts() throws Exception {
        when(orderService.getAllOrders(any())).thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 50), 1));

        mockMvc.perform(get("/order").param("page", "0").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Test Order"))
                .andExpect(jsonPath("$.content[0].customer.name").value("John Doe"))
                .andExpect(jsonPath("$.content[0].products[0].description").value("Keyboard"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getOrderByIdReturnsSpecificOrder() throws Exception {
        when(orderService.getOrder(1L)).thenReturn(order);

        mockMvc.perform(get("/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Test Order"));
    }

    @Test
    void createOrderRejectsMissingProducts() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Test Order\",\"customerId\":1,\"productIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.productIds").exists());
    }
}
