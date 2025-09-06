package com.fooddelivery.order.service;

import com.fooddelivery.order.entity.Order;
import com.fooddelivery.order.entity.OrderItem;
import com.fooddelivery.order.repository.OrderRepository;
import com.fooddelivery.common.dto.OrderDto;
import com.fooddelivery.common.dto.OrderItemDto;
import com.fooddelivery.common.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public OrderDto createOrder(OrderDto orderDto) {
        Order order = new Order(
            orderDto.getCustomerId(),
            orderDto.getRestaurantId(),
            orderDto.getTotalAmount(),
            orderDto.getDeliveryAddress()
        );

        // Convert and add order items
        if (orderDto.getItems() != null) {
            List<OrderItem> orderItems = orderDto.getItems().stream()
                .map(itemDto -> {
                    OrderItem item = new OrderItem(
                        itemDto.getMenuItemId(),
                        itemDto.getItemName(),
                        itemDto.getQuantity(),
                        itemDto.getPrice()
                    );
                    item.setOrder(order);
                    return item;
                })
                .collect(Collectors.toList());
            order.setItems(orderItems);
        }

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public Optional<OrderDto> getOrderById(Long id) {
        return orderRepository.findById(id)
            .map(this::convertToDto);
    }

    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<OrderDto> getOrdersByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(status);
        order = orderRepository.save(order);
        return convertToDto(order);
    }

    private OrderDto convertToDto(Order order) {
        List<OrderItemDto> itemDtos = null;
        if (order.getItems() != null) {
            itemDtos = order.getItems().stream()
                .map(item -> {
                    OrderItemDto dto = new OrderItemDto();
                    dto.setId(item.getId());
                    dto.setMenuItemId(item.getMenuItemId());
                    dto.setItemName(item.getItemName());
                    dto.setQuantity(item.getQuantity());
                    dto.setPrice(item.getPrice());
                    dto.setTotalPrice(item.getTotalPrice());
                    return dto;
                })
                .collect(Collectors.toList());
        }

        return new OrderDto(
            order.getId(),
            order.getCustomerId(),
            order.getRestaurantId(),
            itemDtos,
            order.getTotalAmount(),
            order.getStatus(),
            order.getOrderTime(),
            order.getDeliveryAddress()
        );
    }
}
