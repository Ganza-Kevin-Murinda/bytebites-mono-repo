package com.restaurant_service.service.impl;

import com.restaurant_service.dto.request.RestaurantRequestDTO;
import com.restaurant_service.dto.response.RestaurantResponseDTO;
import com.restaurant_service.exception.RestaurantNotFoundException;
import com.restaurant_service.mapper.RestaurantMapper;
import com.restaurant_service.model.Restaurant;
import com.restaurant_service.repository.RestaurantRepository;
import com.restaurant_service.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantMapper restaurantMapper;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public RestaurantResponseDTO createRestaurant(RestaurantRequestDTO dto) {
        Restaurant restaurant = Restaurant.builder()
                .name(dto.getName())
                .location(dto.getLocation())
                .type(dto.getType())
                .ownerId(dto.getOwnerId())
                .build();
        Restaurant saved = restaurantRepository.save(restaurant);
        return restaurantMapper.toResponseDTO(saved);
    }

    @Override
    public Optional<Restaurant> getRestaurantByName(String name) {
        return restaurantRepository.findByName(name);
    }

    @Override
    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    @Override
    public Page<RestaurantResponseDTO> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(restaurantMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public RestaurantResponseDTO updateRestaurant(RestaurantRequestDTO dto, Long id) {
        Restaurant restaurant = getRestaurantById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(
                        String.format("A restaurant with the Id '%d' doesn't exist", id)));

        if (dto.getName() != null) {
            restaurant.setName(dto.getName());
        }
        if (dto.getLocation() != null) {
            restaurant.setLocation(dto.getLocation());
        }
        if (dto.getType() != null) {
            restaurant.setType(dto.getType());
        }

        Restaurant updated = restaurantRepository.save(restaurant);
        return restaurantMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRestaurant(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new RestaurantNotFoundException(
                    String.format("A restaurant with the Id '%d' doesn't exist", id));
        }
        restaurantRepository.deleteById(id);
    }
}
