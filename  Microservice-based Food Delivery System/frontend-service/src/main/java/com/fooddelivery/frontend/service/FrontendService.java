package com.fooddelivery.frontend.service;

import com.fooddelivery.common.dto.*;
import com.fooddelivery.common.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

@Service
public class FrontendService {

    private static final Logger logger = LoggerFactory.getLogger(FrontendService.class);
    private final WebClient webClient;
    
    @Value("${api.gateway.url:http://localhost:8080}")
    private String apiGatewayUrl;
    
    @Value("${service.restaurant.url:http://localhost:8081}")
    private String restaurantServiceUrl;
    
    @Value("${service.order.url:http://localhost:8082}")
    private String orderServiceUrl;
    
    @Value("${service.delivery.url:http://localhost:8083}")
    private String deliveryServiceUrl;
    
    @Value("${service.payment.url:http://localhost:8084}")
    private String paymentServiceUrl;

    public FrontendService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    // ==================== RESTAURANT MANAGEMENT ====================
    
    public List<RestaurantDto> getAllRestaurants() {
        try {
            logger.debug("Fetching all restaurants from restaurant service");
            RestaurantDto[] restaurants = webClient.get()
                .uri(restaurantServiceUrl + "/api/restaurants")
                .retrieve()
                .bodyToMono(RestaurantDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} restaurants", restaurants != null ? restaurants.length : 0);
            return Arrays.asList(restaurants != null ? restaurants : new RestaurantDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching restaurants: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching restaurants: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public RestaurantDto getRestaurantById(Long id) {
        try {
            logger.debug("Fetching restaurant with ID: {}", id);
            RestaurantDto restaurant = webClient.get()
                .uri(restaurantServiceUrl + "/api/restaurants/" + id)
                .retrieve()
                .bodyToMono(RestaurantDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched restaurant: {}", id);
            return restaurant;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.warn("Restaurant not found: {}", id);
                return null;
            }
            logger.error("HTTP error fetching restaurant {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching restaurant {}: {}", id, e.getMessage());
            return null;
        }
    }

    public RestaurantDto createRestaurant(RestaurantDto restaurant) {
        try {
            logger.info("Creating new restaurant: {}", restaurant.getName());
            // Set default values if not provided
            if (restaurant.getRating() == null) {
                restaurant.setRating(0.0);
            }
            if (restaurant.getIsActive() == null) {
                restaurant.setIsActive(true);
            }
            
            RestaurantDto createdRestaurant = webClient.post()
                .uri(restaurantServiceUrl + "/api/restaurants")
                .header("Content-Type", "application/json")
                .bodyValue(restaurant)
                .retrieve()
                .bodyToMono(RestaurantDto.class)
                .timeout(Duration.ofSeconds(15))
                .block();
            logger.info("Successfully created restaurant with ID: {}", createdRestaurant != null ? createdRestaurant.getId() : "unknown");
            return createdRestaurant;
        } catch (WebClientResponseException e) {
            logger.error("HTTP error creating restaurant: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not create restaurant: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error creating restaurant: {}", e.getMessage());
            throw new RuntimeException("Could not create restaurant: " + e.getMessage());
        }
    }

    public RestaurantDto updateRestaurant(RestaurantDto restaurant) {
        try {
            logger.info("Updating restaurant: {}", restaurant.getId());
            // Ensure required fields have default values
            if (restaurant.getRating() == null) {
                restaurant.setRating(0.0);
            }
            if (restaurant.getIsActive() == null) {
                restaurant.setIsActive(true);
            }
            
            RestaurantDto updatedRestaurant = webClient.put()
                .uri(restaurantServiceUrl + "/api/restaurants/" + restaurant.getId())
                .header("Content-Type", "application/json")
                .bodyValue(restaurant)
                .retrieve()
                .bodyToMono(RestaurantDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully updated restaurant: {}", restaurant.getId());
            return updatedRestaurant;
        } catch (WebClientResponseException e) {
            logger.error("HTTP error updating restaurant: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not update restaurant: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error updating restaurant: {}", e.getMessage());
            throw new RuntimeException("Could not update restaurant: " + e.getMessage());
        }
    }

    public void deleteRestaurant(Long id) {
        try {
            logger.info("Deleting restaurant: {}", id);
            webClient.delete()
                .uri(restaurantServiceUrl + "/api/restaurants/" + id)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully deleted restaurant: {}", id);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error deleting restaurant: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not delete restaurant: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error deleting restaurant: {}", e.getMessage());
            throw new RuntimeException("Could not delete restaurant: " + e.getMessage());
        }
    }

    // ==================== ORDER MANAGEMENT ====================
    
    public List<OrderDto> getAllOrders() {
        try {
            logger.debug("Fetching all orders from order service");
            OrderDto[] orders = webClient.get()
                .uri(orderServiceUrl + "/api/orders")
                .retrieve()
                .bodyToMono(OrderDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} orders", orders != null ? orders.length : 0);
            return Arrays.asList(orders != null ? orders : new OrderDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching orders: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching orders: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public OrderDto getOrderById(Long id) {
        try {
            logger.debug("Fetching order with ID: {}", id);
            OrderDto order = webClient.get()
                .uri(orderServiceUrl + "/api/orders/" + id)
                .retrieve()
                .bodyToMono(OrderDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched order: {}", id);
            return order;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.warn("Order not found: {}", id);
                return null;
            }
            logger.error("HTTP error fetching order {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching order {}: {}", id, e.getMessage());
            return null;
        }
    }

    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        try {
            logger.debug("Fetching orders for customer: {}", customerId);
            OrderDto[] orders = webClient.get()
                .uri(orderServiceUrl + "/api/orders/customer/" + customerId)
                .retrieve()
                .bodyToMono(OrderDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} orders for customer {}", orders != null ? orders.length : 0, customerId);
            return Arrays.asList(orders != null ? orders : new OrderDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching customer orders: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching customer orders: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<OrderDto> getOrdersByRestaurantId(Long restaurantId) {
        try {
            logger.debug("Fetching orders for restaurant: {}", restaurantId);
            OrderDto[] orders = webClient.get()
                .uri(orderServiceUrl + "/api/orders/restaurant/" + restaurantId)
                .retrieve()
                .bodyToMono(OrderDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} orders for restaurant {}", orders != null ? orders.length : 0, restaurantId);
            return Arrays.asList(orders != null ? orders : new OrderDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching restaurant orders: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching restaurant orders: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        try {
            logger.debug("Fetching orders with status: {}", status);
            OrderDto[] orders = webClient.get()
                .uri(orderServiceUrl + "/api/orders/status/" + status)
                .retrieve()
                .bodyToMono(OrderDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} orders with status {}", orders != null ? orders.length : 0, status);
            return Arrays.asList(orders != null ? orders : new OrderDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching orders by status: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching orders by status: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public OrderDto createOrder(OrderDto order) {
        try {
            logger.info("Creating new order for customer: {}", order.getCustomerId());
            OrderDto createdOrder = webClient.post()
                .uri(orderServiceUrl + "/api/orders")
                .header("Content-Type", "application/json")
                .bodyValue(order)
                .retrieve()
                .bodyToMono(OrderDto.class)
                .timeout(Duration.ofSeconds(15))
                .block();
            logger.info("Successfully created order with ID: {}", createdOrder != null ? createdOrder.getId() : "unknown");
            return createdOrder;
        } catch (WebClientResponseException e) {
            logger.error("HTTP error creating order: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not create order: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage());
            throw new RuntimeException("Could not create order: " + e.getMessage());
        }
    }

    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        try {
            logger.info("Updating order {} status to {}", orderId, status);
            OrderDto updatedOrder = webClient.put()
                .uri(orderServiceUrl + "/api/orders/" + orderId + "/status")
                .header("Content-Type", "application/json")
                .bodyValue("{\"status\":\"" + status + "\"}")
                .retrieve()
                .bodyToMono(OrderDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully updated order {} status to {}", orderId, status);
            return updatedOrder;
        } catch (WebClientResponseException e) {
            logger.error("HTTP error updating order status: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not update order status: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error updating order status: {}", e.getMessage());
            throw new RuntimeException("Could not update order status: " + e.getMessage());
        }
    }

    public OrderDto cancelOrder(Long orderId, String reason) {
        try {
            logger.info("Cancelling order {} with reason: {}", orderId, reason);
            OrderDto cancelledOrder = webClient.put()
                .uri(orderServiceUrl + "/api/orders/" + orderId + "/cancel?reason=" + (reason != null ? reason : "Customer requested"))
                .retrieve()
                .bodyToMono(OrderDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully cancelled order: {}", orderId);
            return cancelledOrder;
        } catch (WebClientResponseException e) {
            logger.error("HTTP error cancelling order: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not cancel order: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error cancelling order: {}", e.getMessage());
            throw new RuntimeException("Could not cancel order: " + e.getMessage());
        }
    }

    // ==================== DELIVERY MANAGEMENT ====================
    
    public List<DeliveryDto> getAllDeliveries() {
        try {
            logger.debug("Fetching all deliveries from delivery service");
            DeliveryDto[] deliveries = webClient.get()
                .uri(deliveryServiceUrl + "/api/deliveries")
                .retrieve()
                .bodyToMono(DeliveryDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} deliveries", deliveries != null ? deliveries.length : 0);
            return Arrays.asList(deliveries != null ? deliveries : new DeliveryDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching deliveries: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching deliveries: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public DeliveryDto getDeliveryById(Long id) {
        try {
            logger.debug("Fetching delivery with ID: {}", id);
            DeliveryDto delivery = webClient.get()
                .uri(deliveryServiceUrl + "/api/deliveries/" + id)
                .retrieve()
                .bodyToMono(DeliveryDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched delivery: {}", id);
            return delivery;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.warn("Delivery not found: {}", id);
                return null;
            }
            logger.error("HTTP error fetching delivery {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching delivery {}: {}", id, e.getMessage());
            return null;
        }
    }

    public List<DeliveryDto> getDeliveriesByOrderId(Long orderId) {
        try {
            logger.debug("Fetching deliveries for order: {}", orderId);
            DeliveryDto[] deliveries = webClient.get()
                .uri(deliveryServiceUrl + "/api/deliveries/order/" + orderId)
                .retrieve()
                .bodyToMono(DeliveryDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} deliveries for order {}", deliveries != null ? deliveries.length : 0, orderId);
            return Arrays.asList(deliveries != null ? deliveries : new DeliveryDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching order deliveries: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching order deliveries: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==================== PAYMENT MANAGEMENT ====================
    
    public List<PaymentDto> getAllPayments() {
        try {
            logger.debug("Fetching all payments from payment service");
            PaymentDto[] payments = webClient.get()
                .uri(paymentServiceUrl + "/api/payments")
                .retrieve()
                .bodyToMono(PaymentDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} payments", payments != null ? payments.length : 0);
            return Arrays.asList(payments != null ? payments : new PaymentDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching payments: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching payments: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PaymentDto getPaymentById(Long id) {
        try {
            logger.debug("Fetching payment with ID: {}", id);
            PaymentDto payment = webClient.get()
                .uri(paymentServiceUrl + "/api/payments/" + id)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched payment: {}", id);
            return payment;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.warn("Payment not found: {}", id);
                return null;
            }
            logger.error("HTTP error fetching payment {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching payment {}: {}", id, e.getMessage());
            return null;
        }
    }

    public List<PaymentDto> getPaymentsByOrderId(Long orderId) {
        try {
            logger.debug("Fetching payments for order: {}", orderId);
            PaymentDto[] payments = webClient.get()
                .uri(paymentServiceUrl + "/api/payments/order/" + orderId)
                .retrieve()
                .bodyToMono(PaymentDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} payments for order {}", payments != null ? payments.length : 0, orderId);
            return Arrays.asList(payments != null ? payments : new PaymentDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching order payments: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching order payments: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PaymentDto processPayment(PaymentDto payment) {
        try {
            logger.info("Processing payment for order: {}", payment.getOrderId());
            PaymentDto processedPayment = webClient.post()
                .uri(paymentServiceUrl + "/api/payments")
                .header("Content-Type", "application/json")
                .bodyValue(payment)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .timeout(Duration.ofSeconds(15))
                .block();
            logger.info("Successfully processed payment with ID: {}", processedPayment != null ? processedPayment.getId() : "unknown");
            return processedPayment;
        } catch (WebClientResponseException e) {
            logger.error("HTTP error processing payment: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not process payment: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage());
            throw new RuntimeException("Could not process payment: " + e.getMessage());
        }
    }

    // ==================== MENU ITEM MANAGEMENT ====================
    
    public RestaurantDto getRestaurant(Long restaurantId) {
        // This method delegates to getRestaurantById for consistency
        return getRestaurantById(restaurantId);
    }

    public List<MenuItemDto> getMenuItems(Long restaurantId) {
        try {
            logger.debug("Fetching menu items for restaurant: {}", restaurantId);
            MenuItemDto[] menuItems = webClient.get()
                .uri(restaurantServiceUrl + "/api/restaurants/" + restaurantId + "/menu")
                .retrieve()
                .bodyToMono(MenuItemDto[].class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched {} menu items for restaurant {}", menuItems != null ? menuItems.length : 0, restaurantId);
            return Arrays.asList(menuItems != null ? menuItems : new MenuItemDto[0]);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching menu items: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error fetching menu items: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public MenuItemDto getMenuItemById(Long restaurantId, Long itemId) {
        try {
            logger.debug("Fetching menu item {} for restaurant: {}", itemId, restaurantId);
            MenuItemDto menuItem = webClient.get()
                .uri(restaurantServiceUrl + "/api/restaurants/" + restaurantId + "/menu/" + itemId)
                .retrieve()
                .bodyToMono(MenuItemDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully fetched menu item: {}", itemId);
            return menuItem;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.warn("Menu item not found: {} in restaurant {}", itemId, restaurantId);
                return null;
            }
            logger.error("HTTP error fetching menu item: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching menu item: {}", e.getMessage());
            return null;
        }
    }

    public MenuItemDto createMenuItem(Long restaurantId, MenuItemDto menuItem) {
        try {
            logger.info("Creating menu item '{}' for restaurant: {}", menuItem.getName(), restaurantId);
            MenuItemDto createdItem = webClient.post()
                .uri(restaurantServiceUrl + "/api/restaurants/" + restaurantId + "/menu")
                .header("Content-Type", "application/json")
                .bodyValue(menuItem)
                .retrieve()
                .bodyToMono(MenuItemDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully created menu item with ID: {}", createdItem != null ? createdItem.getId() : "unknown");
            return createdItem;
        } catch (WebClientResponseException e) {
            logger.error("HTTP error creating menu item: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not create menu item: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error creating menu item: {}", e.getMessage());
            throw new RuntimeException("Could not create menu item: " + e.getMessage());
        }
    }

    public MenuItemDto updateMenuItem(Long restaurantId, Long itemId, MenuItemDto menuItem) {
        try {
            logger.info("Updating menu item {} for restaurant: {}", itemId, restaurantId);
            MenuItemDto updatedItem = webClient.put()
                .uri(restaurantServiceUrl + "/api/restaurants/" + restaurantId + "/menu/" + itemId)
                .header("Content-Type", "application/json")
                .bodyValue(menuItem)
                .retrieve()
                .bodyToMono(MenuItemDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully updated menu item: {}", itemId);
            return updatedItem;
        } catch (WebClientResponseException e) {
            logger.error("HTTP error updating menu item: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not update menu item: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error updating menu item: {}", e.getMessage());
            throw new RuntimeException("Could not update menu item: " + e.getMessage());
        }
    }

    public void deleteMenuItem(Long restaurantId, Long itemId) {
        try {
            logger.info("Deleting menu item {} from restaurant: {}", itemId, restaurantId);
            webClient.delete()
                .uri(restaurantServiceUrl + "/api/restaurants/" + restaurantId + "/menu/" + itemId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            logger.info("Successfully deleted menu item: {}", itemId);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error deleting menu item: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Could not delete menu item: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error deleting menu item: {}", e.getMessage());
            throw new RuntimeException("Could not delete menu item: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================
    
    public boolean isServiceHealthy(String serviceName) {
        try {
            String serviceUrl = getServiceUrl(serviceName);
            webClient.get()
                .uri(serviceUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            logger.debug("Service {} is healthy", serviceName);
            return true;
        } catch (Exception e) {
            logger.warn("Service {} health check failed: {}", serviceName, e.getMessage());
            return false;
        }
    }
    
    private String getServiceUrl(String serviceName) {
        switch (serviceName.toLowerCase()) {
            case "restaurant": return restaurantServiceUrl;
            case "order": return orderServiceUrl;
            case "delivery": return deliveryServiceUrl;
            case "payment": return paymentServiceUrl;
            default: return apiGatewayUrl;
        }
    }
}
