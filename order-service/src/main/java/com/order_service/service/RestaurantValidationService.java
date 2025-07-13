package com.order_service.service;

import com.order_service.dto.request.OrderItemRequestDTO;
import com.order_service.dto.request.OrderRequestDTO;
import com.order_service.dto.response.MenuValidationResponseDTO;
import com.order_service.dto.response.RestaurantValidationResponseDTO;
import com.order_service.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantValidationService {

    private final RestaurantServiceClient restaurantServiceClient;

    public void validateOrderRequest(OrderRequestDTO orderRequestDTO) {
        log.info("Starting validation for order request - Restaurant ID: {}", orderRequestDTO.getRestaurantId());

        try {
            // 1. Validate restaurant
            RestaurantValidationResponseDTO restaurant = validateRestaurant(orderRequestDTO.getRestaurantId());

            // 2. Validate menu items
            List<Long> menuItemIds = orderRequestDTO.getItems().stream()
                    .map(OrderItemRequestDTO::getMenuItemId)
                    .collect(Collectors.toList());

            List<MenuValidationResponseDTO> validMenuItems = validateMenuItems(
                    orderRequestDTO.getRestaurantId(),
                    menuItemIds
            );

            // 3. Validate prices and availability
            validatePricesAndAvailability(orderRequestDTO.getItems(), validMenuItems);

            log.info("Validation completed successfully for restaurant: {}", restaurant.getName());

        } catch (Exception e) {
            log.error("Validation failed for order request", e);
            throw e;
        }
    }

    private RestaurantValidationResponseDTO validateRestaurant(Long restaurantId) {
        try {
            CompletableFuture<RestaurantValidationResponseDTO> future =
                    restaurantServiceClient.validateRestaurant(restaurantId);

            RestaurantValidationResponseDTO restaurant = future.get(5, TimeUnit.SECONDS);

            if (!restaurant.isActive()) {
                throw new RestaurantNotFoundException("Restaurant is not active: " + restaurantId);
            }

            return restaurant;

        } catch (TimeoutException e) {
            throw new RestaurantServiceCommunicationException("Timeout while validating restaurant: " + restaurantId);
        } catch (InterruptedException | ExecutionException e) {
            throw new RestaurantServiceCommunicationException("Error validating restaurant: " + restaurantId, e);
        }
    }

    private List<MenuValidationResponseDTO> validateMenuItems(Long restaurantId, List<Long> menuItemIds) {
        try {
            CompletableFuture<List<MenuValidationResponseDTO>> future =
                    restaurantServiceClient.validateMenuItems(restaurantId, menuItemIds);

            List<MenuValidationResponseDTO> validMenuItems = future.get(5, TimeUnit.SECONDS);

            // Check if all requested menu items were found
            if (validMenuItems.size() != menuItemIds.size()) {
                Set<Long> foundIds = validMenuItems.stream()
                        .map(MenuValidationResponseDTO::getId)
                        .collect(Collectors.toSet());

                List<Long> missingIds = menuItemIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .toList();

                throw new MenuItemNotFoundException("Menu items not found: " + missingIds);
            }

            return validMenuItems;

        } catch (TimeoutException e) {
            throw new RestaurantServiceCommunicationException("Timeout while validating menu items for restaurant: " + restaurantId);
        } catch (InterruptedException | ExecutionException e) {
            throw new RestaurantServiceCommunicationException("Error validating menu items for restaurant: " + restaurantId, e);
        }
    }

    private void validatePricesAndAvailability(List<OrderItemRequestDTO> requestItems,
                                               List<MenuValidationResponseDTO> validMenuItems) {

        Map<Long, MenuValidationResponseDTO> menuItemMap = validMenuItems.stream()
                .collect(Collectors.toMap(MenuValidationResponseDTO::getId, item -> item));

        for (OrderItemRequestDTO requestItem : requestItems) {
            MenuValidationResponseDTO menuItem = menuItemMap.get(requestItem.getMenuItemId());

            // Check availability
            if (!menuItem.isAvailable()) {
                throw new MenuItemNotAvailableException(
                        "Menu item is not available: " + menuItem.getName()
                );
            }

            // Check price
            if (requestItem.getUnitPrice().compareTo(menuItem.getPrice()) != 0) {
                throw new PriceValidationException(
                        String.format("Price mismatch for item '%s'. Expected: %s, Provided: %s",
                                menuItem.getName(), menuItem.getPrice(), requestItem.getUnitPrice())
                );
            }
        }
    }
}
