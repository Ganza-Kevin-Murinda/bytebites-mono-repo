package com.restaurant_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantRequestDTO {
    String name;
    String type;
    String location;
    String ownerId;
}
