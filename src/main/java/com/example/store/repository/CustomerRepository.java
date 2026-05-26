package com.example.store.repository;

import com.example.store.entity.Customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query(value = "select c.id from Customer c", countQuery = "select count(c) from Customer c")
    Page<Long> findPageIds(Pageable pageable);

    @Query(
            value =
                    """
                    select c.id
                    from Customer c
                    where lower(c.name) like lower(concat('%', :query, '%'))
                    """,
            countQuery =
                    """
                    select count(c)
                    from Customer c
                    where lower(c.name) like lower(concat('%', :query, '%'))
                    """)
    Page<Long> searchPageIdsByNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query(
            """
            select distinct c
            from Customer c
            left join fetch c.orders
            """)
    List<Customer> findAllWithOrders();

    @Query(
            """
            select distinct c
            from Customer c
            left join fetch c.orders
            where c.id in :ids
            """)
    List<Customer> findAllWithOrdersByIdIn(@Param("ids") List<Long> ids);

    @Query(
            """
            select distinct c
            from Customer c
            left join fetch c.orders
            where c.id = :id
            """)
    Optional<Customer> findByIdWithOrders(@Param("id") Long id);

    @Query(
            """
            select distinct c
            from Customer c
            left join fetch c.orders
            where lower(c.name) like lower(concat('%', :query, '%'))
            """)
    List<Customer> searchByNameContainingIgnoreCase(@Param("query") String query);
}
