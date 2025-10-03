package com.fooddelivery.order.controller;

import com.fooddelivery.order.dto.*;
import com.fooddelivery.common.enums.OrderStatus;
import com.fooddelivery.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Enhanced REST Controller for Order Management
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    /**
     * Get all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        logger.debug("Retrieving all orders");
        List<OrderResponseDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        logger.debug("Retrieving order with ID: {}", id);
        return orderService.getOrderById(id)
                .map(order -> ResponseEntity.ok(order))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new order
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequest) {
        logger.info("Creating new order for customer: {}", orderRequest.getCustomerId());
        try {
            OrderResponseDto createdOrder = orderService.createOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update order status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id, 
            @Valid @RequestBody OrderStatusUpdateDto statusUpdate) {
        logger.info("Updating order {} status to {}", id, statusUpdate.getStatus());
        try {
            OrderResponseDto updatedOrder = orderService.updateOrderStatus(id, statusUpdate);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            logger.error("Error updating order status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel an order
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        logger.info("Cancelling order: {}", id);
        try {
            OrderResponseDto cancelledOrder = orderService.cancelOrder(id, reason);
            return ResponseEntity.ok(cancelledOrder);
        } catch (Exception e) {
            logger.error("Error cancelling order: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get orders by customer ID
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByCustomerId(@PathVariable Long customerId) {
        logger.debug("Retrieving orders for customer: {}", customerId);
        List<OrderResponseDto> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by restaurant ID
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByRestaurantId(@PathVariable Long restaurantId) {
        logger.debug("Retrieving orders for restaurant: {}", restaurantId);
        List<OrderResponseDto> orders = orderService.getOrdersByRestaurantId(restaurantId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByStatus(@PathVariable OrderStatus status) {
        logger.debug("Retrieving orders with status: {}", status);
        List<OrderResponseDto> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Order Service is running");
    }
}
