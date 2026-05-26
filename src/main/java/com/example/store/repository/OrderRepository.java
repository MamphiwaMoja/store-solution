package com.example.store.repository;

import com.example.store.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = "select o.id from StoreOrder o", countQuery = "select count(o) from StoreOrder o")
    Page<Long> findPageIds(Pageable pageable);

    @Query(
            """
            select distinct o
            from StoreOrder o
            left join fetch o.customer
            left join fetch o.products
            """)
    List<Order> findAllWithDetails();

    @Query(
            """
            select distinct o
            from StoreOrder o
            left join fetch o.customer
            left join fetch o.products
            where o.id in :ids
            """)
    List<Order> findAllWithDetailsByIdIn(@Param("ids") List<Long> ids);

    @Query(
            """
            select distinct o
            from StoreOrder o
            left join fetch o.customer
            left join fetch o.products
            where o.id = :id
            """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}
