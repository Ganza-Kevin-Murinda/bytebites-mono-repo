package com.order_service.service;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import com.order_service.dto.request.MenuValidationRequestDTO;
import com.order_service.dto.response.ApiResponseDTO;
import com.order_service.dto.response.MenuValidationResponseDTO;
import com.order_service.dto.response.RestaurantValidationResponseDTO;
import com.order_service.exception.RestaurantServiceCommunicationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceClient {
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    private static final String RESTAURANT_SERVICE_NAME = "restaurant-service";
    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "fallbackValidateRestaurant")
    @Retry(name = "default")
    @TimeLimiter(name = "default")
    public CompletableFuture<RestaurantValidationResponseDTO> validateRestaurant(Long restaurantId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = "http://api-gateway/api/restaurants/internal/validate/" + restaurantId;

                log.info("Validating restaurant with ID: {} via API Gateway at: {}", restaurantId, url);

                ResponseEntity<ApiResponseDTO<RestaurantValidationResponseDTO>> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<>() {}
                        );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody().getData();
                } else {
                    throw new RestaurantServiceCommunicationException("Failed to validate restaurant: " + restaurantId);
                }


            } catch (Exception e) {
                log.error("Error validating restaurant with ID: {}", restaurantId, e);
                throw new RestaurantServiceCommunicationException("Failed to communicate with restaurant service", e);
            }
        });
    }

    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "fallbackValidateMenuItems")
    @Retry(name = "default")
    @TimeLimiter(name = "default")
    public CompletableFuture<List<MenuValidationResponseDTO>> validateMenuItems(Long restaurantId, List<Long> menuItemIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = "http://api-gateway/api/restaurants/internal/" + restaurantId + "/menus/validate";

                log.info("Validating menu items for restaurant: {} with items: {}", restaurantId, menuItemIds);

                MenuValidationRequestDTO request = MenuValidationRequestDTO.builder()
                        .menuItemIds(menuItemIds)
                        .build();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<MenuValidationRequestDTO> entity = new HttpEntity<>(request, headers);

                ResponseEntity<ApiResponseDTO<List<MenuValidationResponseDTO>>> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.POST,
                                entity,
                                new ParameterizedTypeReference<>() {
                                }
                        );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody().getData();
                } else {
                    throw new RestaurantServiceCommunicationException("Failed to validate menu items for restaurant: " + restaurantId);
                }

            } catch (Exception e) {
                log.error("Error validating menu items for restaurant: {} with items: {}", restaurantId, menuItemIds, e);
                throw new RestaurantServiceCommunicationException("Failed to communicate with restaurant service", e);
            }
        });
    }

//    private String getServiceUrl() {
//        List<ServiceInstance> instances = discoveryClient.getInstances(RESTAURANT_SERVICE_NAME);
//        if (instances.isEmpty()) {
//            throw new RestaurantServiceCommunicationException("Restaurant service not available");
//        }
//
//        ServiceInstance instance = instances.getFirst();
//        return instance.getUri().toString();
//    }

    // Fallback methods
    public CompletableFuture<RestaurantValidationResponseDTO> fallbackValidateRestaurant(Long restaurantId, Exception ex) {
        log.error("Fallback triggered for restaurant validation. Restaurant ID: {}", restaurantId, ex);
        throw new RestaurantServiceCommunicationException("Restaurant service is currently unavailable. Please try again later.");
    }

    public CompletableFuture<List<MenuValidationResponseDTO>> fallbackValidateMenuItems(Long restaurantId, List<Long> menuItemIds, Exception ex) {
        log.error("Fallback triggered for menu items validation. Restaurant ID: {}, Menu Items: {}", restaurantId, menuItemIds, ex);
        throw new RestaurantServiceCommunicationException("Restaurant service is currently unavailable. Please try again later.");
    }
}
