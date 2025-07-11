package com.restaurant_service.mapper;

import com.restaurant_service.dto.response.MenuResponseDTO;
import com.restaurant_service.model.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = RestaurantMapper.class)
public interface MenuMapper {

    @Mapping(source = "restaurant", target = "restaurantSummaryDto")
    MenuResponseDTO toResponseDTO(Menu menu);

}
