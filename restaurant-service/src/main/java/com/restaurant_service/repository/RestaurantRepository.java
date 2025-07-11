package com.restaurant_service.repository;

import com.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
    Optional<Restaurant> findByName(String name);
}
