package com.restaurant_service.service;

import com.restaurant_service.dto.request.RestaurantRequestDTO;
import com.restaurant_service.dto.response.RestaurantResponseDTO;
import com.restaurant_service.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.Optional;
@Service
public interface RestaurantService {

    RestaurantResponseDTO createRestaurant(RestaurantRequestDTO restaurantRequestDto) throws Exception;

    Optional<Restaurant> getRestaurantByName(String name);

    Optional<Restaurant> getRestaurantById(Long id);

    Page<RestaurantResponseDTO> getAllRestaurants(Pageable pageable);

    RestaurantResponseDTO updateRestaurant(RestaurantRequestDTO restaurantRequestDto, Long restaurantId);

    void deleteRestaurant(Long id);
}
