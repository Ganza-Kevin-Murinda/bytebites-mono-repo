package com.order_service.service;

import com.order_service.dto.request.OrderRequestDTO;
import com.order_service.dto.response.OrderResponseDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO, String customerId);

    Optional<OrderResponseDTO> getOrderById(Long orderId, String customerId);

    List<OrderResponseDTO> getOrdersByCustomer(String customerId);

    List<OrderResponseDTO> getOrdersByRestaurant(Long restaurantId);

    Optional<OrderResponseDTO> getOrderByIdForRestaurant(Long orderId, Long restaurantId);
}
