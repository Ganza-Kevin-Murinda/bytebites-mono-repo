package com.restaurant_service.mapper;

import com.restaurant_service.dto.response.RestaurantResponseDTO;
import com.restaurant_service.dto.response.RestaurantSummaryDTO;
import com.restaurant_service.model.Restaurant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    RestaurantResponseDTO toResponseDTO(Restaurant restaurant);
    RestaurantSummaryDTO toSummaryDTO(Restaurant restaurant);
}
