package com.restaurant_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponseDTO {
    String name;
    double price;
    RestaurantSummaryDTO restaurantSummaryDto;
}
