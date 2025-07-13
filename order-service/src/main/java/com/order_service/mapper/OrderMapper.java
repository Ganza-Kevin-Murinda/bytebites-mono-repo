package com.order_service.mapper;

import com.order_service.dto.request.OrderItemRequestDTO;
import com.order_service.dto.response.OrderItemResponseDTO;
import com.order_service.dto.request.OrderRequestDTO;
import com.order_service.dto.response.OrderResponseDTO;
import com.order_service.model.Order;
import com.order_service.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequestDTO orderRequestDTO);

    OrderResponseDTO toResponseDTO(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "totalPrice", source = ".", qualifiedByName = "calculateTotalPrice")
    OrderItem toEntity(OrderItemRequestDTO orderItemRequestDTO);

    OrderItemResponseDTO toResponseDTO(OrderItem orderItem);

    List<OrderItem> toEntityList(List<OrderItemRequestDTO> orderItemRequestDTOs);

    List<OrderItemResponseDTO> toResponseDTOList(List<OrderItem> orderItems);

    @Named("calculateTotalPrice")
    default BigDecimal calculateTotalPrice(OrderItemRequestDTO orderItemRequestDTO) {
        return orderItemRequestDTO.getUnitPrice()
                .multiply(BigDecimal.valueOf(orderItemRequestDTO.getQuantity()));
    }
}
