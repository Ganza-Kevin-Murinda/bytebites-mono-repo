package com.order_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuValidationRequestDTO {
    @NotEmpty(message = "Menu item IDs cannot be empty")
    private List<Long> menuItemIds;
}