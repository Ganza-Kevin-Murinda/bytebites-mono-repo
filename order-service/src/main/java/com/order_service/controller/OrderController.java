package com.order_service.controller;

import com.order_service.dto.request.OrderRequestDTO;
import com.order_service.dto.response.OrderResponseDTO;
import com.order_service.exception.OrderNotFoundException;
import com.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing food orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new order", description = "Place a new food order (Customer only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Customer role required")
    })
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequestDTO,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String customerId) {

        log.info("Creating order for customer: {}", customerId);
        OrderResponseDTO createdOrder = orderService.createOrder(orderRequestDTO, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get order by ID", description = "Get order details by ID (Customer can only see their own orders)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Customer role required"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String customerId) {

        log.info("Fetching order {} for customer {}", orderId, customerId);
        OrderResponseDTO order = orderService.getOrderById(orderId, customerId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get customer's orders", description = "Get all orders for the authenticated customer",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Customer role required")
    })
    public ResponseEntity<List<OrderResponseDTO>> getCustomerOrders(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String customerId) {

        log.info("Fetching orders for customer: {}", customerId);
        List<OrderResponseDTO> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get restaurant's orders", description = "Get all orders for a specific restaurant (Restaurant Owner only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Restaurant Owner role required")
    })
    public ResponseEntity<List<OrderResponseDTO>> getRestaurantOrders(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {

        log.info("Fetching orders for restaurant {} by user {}", restaurantId, userId);
        List<OrderResponseDTO> orders = orderService.getOrdersByRestaurant(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get order by ID for restaurant", description = "Get order details by ID for restaurant owners",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Restaurant Owner role required"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponseDTO> getOrderByIdForRestaurant(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {

        log.info("Fetching order {} for restaurant {} by user {}", orderId, restaurantId, userId);
        OrderResponseDTO order = orderService.getOrderByIdForRestaurant(orderId, restaurantId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return ResponseEntity.ok(order);
    }
}
