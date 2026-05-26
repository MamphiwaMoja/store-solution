package com.example.store.service;

import com.example.store.dto.request.CreateCustomerRequest;
import com.example.store.dto.request.CreateOrderRequest;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.service.impl.CustomerServiceImpl;
import com.example.store.service.impl.OrderServiceImpl;
import com.example.store.service.impl.ProductServiceImpl;
import com.example.store.web.rest.errors.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    void createCustomerTrimsNameBeforeSaving() {
        CustomerService customerService = new CustomerServiceImpl(customerRepository);
        Customer saved = new Customer();
        saved.setId(1L);
        saved.setName("Jane Doe");
        when(customerRepository.save(org.mockito.ArgumentMatchers.any(Customer.class)))
                .thenReturn(saved);

        customerService.createCustomer(new CreateCustomerRequest(" Jane Doe "));

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertThat(customerCaptor.getValue().getName()).isEqualTo("Jane Doe");
    }

    @Test
    void getProductThrowsWhenProductDoesNotExist() {
        ProductService productService = new ProductServiceImpl(productRepository);
        when(productRepository.findByIdWithOrders(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product 99 was not found");
    }

    @Test
    void createOrderRejectsMissingProductIds() {
        OrderService orderService = new OrderServiceImpl(orderRepository, customerRepository, productRepository);
        Customer customer = new Customer();
        customer.setId(1L);
        Product product = new Product();
        product.setId(7L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findAllById(List.of(7L, 99L))).thenReturn(List.of(product));

        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest("Office setup", 1L, List.of(7L, 99L))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product IDs were not found: [99]");
    }

    @Test
    void createOrderPersistsCustomerProductsAndTrimmedDescription() {
        OrderService orderService = new OrderServiceImpl(orderRepository, customerRepository, productRepository);
        Customer customer = new Customer();
        customer.setId(1L);
        Product product = new Product();
        product.setId(7L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findAllById(List.of(7L))).thenReturn(List.of(product));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order created = orderService.createOrder(new CreateOrderRequest(" Office setup ", 1L, List.of(7L)));

        assertThat(created.getDescription()).isEqualTo("Office setup");
        assertThat(created.getCustomer()).isSameAs(customer);
        assertThat(created.getProducts()).containsExactly(product);
    }
}
