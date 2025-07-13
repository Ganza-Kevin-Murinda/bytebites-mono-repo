package com.order_service.repository;

import com.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id AND o.customerId = :customerId")
    Optional<Order> findByIdAndCustomerId(@Param("id") Long id, @Param("customerId") String customerId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id AND o.restaurantId = :restaurantId")
    Optional<Order> findByIdAndRestaurantId(@Param("id") Long id, @Param("restaurantId") Long restaurantId);

}
