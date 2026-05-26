package com.example.store.repository;

import com.example.store.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = "select p.id from Product p", countQuery = "select count(p) from Product p")
    Page<Long> findPageIds(Pageable pageable);

    @Query(
            """
            select distinct p
            from Product p
            left join fetch p.orders
            """)
    List<Product> findAllWithOrders();

    @Query(
            """
            select distinct p
            from Product p
            left join fetch p.orders
            where p.id in :ids
            """)
    List<Product> findAllWithOrdersByIdIn(@Param("ids") List<Long> ids);

    @Query(
            """
            select distinct p
            from Product p
            left join fetch p.orders
            where p.id = :id
            """)
    Optional<Product> findByIdWithOrders(@Param("id") Long id);
}
