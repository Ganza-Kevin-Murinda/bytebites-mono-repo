package com.order_service.service.impl;

import com.order_service.dto.request.OrderRequestDTO;
import com.order_service.dto.response.OrderResponseDTO;
import com.order_service.exception.InvalidOrderException;
import com.order_service.mapper.OrderMapper;
import com.order_service.model.Order;
import com.order_service.model.OrderItem;
import com.order_service.model.OrderStatus;
import com.order_service.repository.OrderRepository;
import com.order_service.service.OrderService;
import com.order_service.service.RestaurantValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final RestaurantValidationService restaurantValidationService;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO, String customerId) {
        log.info("Creating order for customer: {}", customerId);

        // 1. Validate basic order structure
        validateBasicOrder(orderRequestDTO);

        // 2. Validate with restaurant service
        restaurantValidationService.validateOrderRequest(orderRequestDTO);

        // 3. Create order entity
        Order order = orderMapper.toEntity(orderRequestDTO);
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.PENDING);

        // 4. Create order items
        List<OrderItem> orderItems = orderMapper.toEntityList(orderRequestDTO.getItems());
        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        // 5. Calculate total amount
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        // 6. Save order
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return orderMapper.toResponseDTO(savedOrder);
    }

    @Override
    public Optional<OrderResponseDTO> getOrderById(Long orderId, String customerId) {
        log.info("Fetching order {} for customer {}", orderId, customerId);

        return orderRepository.findByIdAndCustomerId(orderId, customerId)
                .map(orderMapper::toResponseDTO);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByCustomer(String customerId) {
        log.info("Fetching orders for customer: {}", customerId);

        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return orders.stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<OrderResponseDTO> getOrdersByRestaurant(Long restaurantId) {
        log.info("Fetching orders for restaurant: {}", restaurantId);

        List<Order> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return orders.stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<OrderResponseDTO> getOrderByIdForRestaurant(Long orderId, Long restaurantId) {
        log.info("Fetching order {} for restaurant {}", orderId, restaurantId);

        return orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .map(orderMapper::toResponseDTO);
    }

    private void validateBasicOrder(OrderRequestDTO orderRequestDTO) {
        if (orderRequestDTO.getItems() == null || orderRequestDTO.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        // Validate each item
        orderRequestDTO.getItems().forEach(item -> {
            if (item.getQuantity() <= 0) {
                throw new InvalidOrderException("Item quantity must be positive");
            }
            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOrderException("Item unit price must be positive");
            }
        });
    }
}
