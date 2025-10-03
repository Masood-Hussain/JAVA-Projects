package com.fooddelivery.order.service;

import com.fooddelivery.order.entity.Order;
import com.fooddelivery.order.entity.OrderItem;
import com.fooddelivery.order.repository.OrderRepository;
import com.fooddelivery.order.dto.*;
import com.fooddelivery.order.exception.OrderException;
import com.fooddelivery.common.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enhanced Order Service with comprehensive business logic
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Create a new order from OrderRequestDto
     */
    public OrderResponseDto createOrder(OrderRequestDto orderRequest) {
        logger.info("Creating new order for customer: {}, restaurant: {}", 
                   orderRequest.getCustomerId(), orderRequest.getRestaurantId());

        // Create order entity
        Order order = new Order();
        order.setCustomerId(orderRequest.getCustomerId());
        order.setRestaurantId(orderRequest.getRestaurantId());
        order.setDeliveryAddress(orderRequest.getDeliveryAddress());
        order.setSpecialInstructions(orderRequest.getSpecialInstructions());
        order.setCustomerPhone(orderRequest.getCustomerPhone());
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setDeliveryFee(orderRequest.getDeliveryFee());
        order.setTaxAmount(orderRequest.getTaxAmount());
        order.setDiscountAmount(orderRequest.getDiscountAmount());
        order.setEstimatedDeliveryTime(orderRequest.getEstimatedDeliveryTime());
        
        // Create order items
        List<OrderItem> orderItems = orderRequest.getItems().stream()
                .map(itemRequest -> {
                    OrderItem item = new OrderItem(
                            itemRequest.getMenuItemId(),
                            itemRequest.getItemName(),
                            itemRequest.getQuantity(),
                            itemRequest.getPrice(),
                            itemRequest.getSpecialInstructions()
                    );
                    item.setOrder(order);
                    return item;
                })
                .collect(Collectors.toList());
        
        order.setItems(orderItems);
        
        // Calculate total amount
        order.recalculateTotal();
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        logger.info("Order created successfully with ID: {}", savedOrder.getId());
        
        return convertToResponseDto(savedOrder);
    }

    /**
     * Get all orders
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        logger.debug("Retrieving all orders");
        return orderRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public Optional<OrderResponseDto> getOrderById(Long id) {
        logger.debug("Retrieving order by ID: {}", id);
        return orderRepository.findById(id)
                .map(this::convertToResponseDto);
    }

    /**
     * Get orders by customer ID
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByCustomerId(Long customerId) {
        logger.debug("Retrieving orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by restaurant ID
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByRestaurantId(Long restaurantId) {
        logger.debug("Retrieving orders for restaurant: {}", restaurantId);
        return orderRepository.findByRestaurantId(restaurantId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(OrderStatus status) {
        logger.debug("Retrieving orders with status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Update order status
     */
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto statusUpdate) {
        logger.info("Updating order {} status to {}", orderId, statusUpdate.getStatus());
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with id: " + orderId));
        
        OrderStatus oldStatus = order.getStatus();
        
        // Validate status transition
        validateStatusTransition(oldStatus, statusUpdate.getStatus());
        
        // Update status
        order.setStatus(statusUpdate.getStatus());
        
        // Handle special status updates
        if (statusUpdate.getStatus() == OrderStatus.DELIVERED) {
            order.markAsDelivered();
            if (statusUpdate.getActualDeliveryTime() != null) {
                order.setActualDeliveryTime(statusUpdate.getActualDeliveryTime());
            }
        }
        
        if (statusUpdate.getEstimatedDeliveryTime() != null) {
            order.setEstimatedDeliveryTime(statusUpdate.getEstimatedDeliveryTime());
        }
        
        Order savedOrder = orderRepository.save(order);
        
        logger.info("Order {} status updated from {} to {}", orderId, oldStatus, statusUpdate.getStatus());
        
        return convertToResponseDto(savedOrder);
    }

    /**
     * Cancel an order
     */
    public OrderResponseDto cancelOrder(Long orderId, String reason) {
        logger.info("Cancelling order: {} with reason: {}", orderId, reason);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with id: " + orderId));
        
        if (!order.canBeCancelled()) {
            throw new OrderException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        
        logger.info("Order {} cancelled successfully", orderId);
        
        return convertToResponseDto(savedOrder);
    }

    /**
     * Convert Order entity to OrderResponseDto
     */
    private OrderResponseDto convertToResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomerId());
        dto.setRestaurantId(order.getRestaurantId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setSpecialInstructions(order.getSpecialInstructions());
        dto.setCustomerPhone(order.getCustomerPhone());
        dto.setCustomerEmail(order.getCustomerEmail());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setOrderTime(order.getOrderTime());
        dto.setUpdatedTime(order.getUpdatedTime());
        dto.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        dto.setActualDeliveryTime(order.getActualDeliveryTime());
        
        // Convert order items
        if (order.getItems() != null) {
            List<OrderItemResponseDto> itemDtos = order.getItems().stream()
                    .map(this::convertToItemResponseDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }
        
        return dto;
    }

    /**
     * Convert OrderItem entity to OrderItemResponseDto
     */
    private OrderItemResponseDto convertToItemResponseDto(OrderItem item) {
        return new OrderItemResponseDto(
                item.getId(),
                item.getMenuItemId(),
                item.getItemName(),
                item.getQuantity(),
                item.getPrice(),
                item.getTotalPrice(),
                item.getSpecialInstructions()
        );
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change
        }
        
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                    throw new OrderException("Invalid status transition from PENDING to " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != OrderStatus.PREPARING && newStatus != OrderStatus.CANCELLED) {
                    throw new OrderException("Invalid status transition from CONFIRMED to " + newStatus);
                }
                break;
            case PREPARING:
                if (newStatus != OrderStatus.READY_FOR_PICKUP && newStatus != OrderStatus.CANCELLED) {
                    throw new OrderException("Invalid status transition from PREPARING to " + newStatus);
                }
                break;
            case READY_FOR_PICKUP:
                if (newStatus != OrderStatus.PICKED_UP && newStatus != OrderStatus.CANCELLED) {
                    throw new OrderException("Invalid status transition from READY_FOR_PICKUP to " + newStatus);
                }
                break;
            case PICKED_UP:
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new OrderException("Invalid status transition from PICKED_UP to " + newStatus);
                }
                break;
            case DELIVERED:
            case CANCELLED:
                throw new OrderException("Cannot change status from " + currentStatus);
            default:
                throw new OrderException("Unknown status: " + currentStatus);
        }
    }
}
