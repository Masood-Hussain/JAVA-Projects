package com.fooddelivery.frontend.controller;

import com.fooddelivery.common.dto.*;
import com.fooddelivery.common.enums.OrderStatus;
import com.fooddelivery.frontend.service.FrontendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class ModernUIController {

    private static final Logger logger = LoggerFactory.getLogger(ModernUIController.class);

    @Value("${api.gateway.url:http://localhost:8080}")
    private String apiGatewayUrl;

    @Autowired
    private FrontendService frontendService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("features", new String[]{
            "AI-Powered Recommendations", 
            "Real-time Order Tracking", 
            "Advanced Payment Options",
            "Smart Menu Search",
            "Cryptocurrency Support",
            "Split Payments",
            "BNPL Options"
        });
        return "index";
    }

    @GetMapping("/restaurants")
    public String restaurants(Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("title", "Discover Amazing Restaurants");
        return "restaurants";
    }

    @GetMapping("/restaurant/{id}")
    public String restaurantDetail(@PathVariable Long id, Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("restaurantId", id);
        model.addAttribute("title", "Restaurant Menu & Details");
        return "restaurant-detail";
    }

    @GetMapping("/menu/{restaurantId}")
    public String advancedMenu(@PathVariable Long restaurantId, Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("title", "Smart Menu Explorer");
        return "advanced-menu";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("title", "Your Order");
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("title", "Secure Checkout");
        model.addAttribute("paymentMethods", new String[]{
            "Credit/Debit Card", "Digital Wallets", "Cryptocurrency", 
            "Bank Transfer", "Buy Now Pay Later", "Loyalty Points"
        });
        return "checkout";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("title", "Order History");
        return "orders";
    }

    @GetMapping("/order/{orderId}/track")
    public String trackOrder(@PathVariable String orderId, Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("orderId", orderId);
        model.addAttribute("title", "Real-time Order Tracking");
        return "order-tracking";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("title", "User Profile");
        return "profile";
    }

    @GetMapping("/payment-methods")
    public String paymentMethods(Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("title", "Payment Methods");
        return "payment-methods";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("title", "Admin Dashboard");
        return "admin";
    }

    @GetMapping("/admin/analytics")
    public String analytics(Model model) {
        model.addAttribute("apiUrl", apiGatewayUrl);
        model.addAttribute("title", "Analytics Dashboard");
        return "analytics";
    }

    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("title", "Help & Support");
        return "help";
    }

    // ==================== REST API ENDPOINTS ====================

    /**
     * API endpoint to get all restaurants
     */
    @GetMapping("/api/restaurants")
    @ResponseBody
    public ResponseEntity<List<RestaurantDto>> getRestaurants() {
        try {
            logger.debug("API request: Get all restaurants");
            List<RestaurantDto> restaurants = frontendService.getAllRestaurants();
            return ResponseEntity.ok(restaurants);
        } catch (Exception e) {
            logger.error("Error fetching restaurants via API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API endpoint to get restaurant by ID
     */
    @GetMapping("/api/restaurants/{id}")
    @ResponseBody
    public ResponseEntity<RestaurantDto> getRestaurant(@PathVariable Long id) {
        try {
            logger.debug("API request: Get restaurant {}", id);
            RestaurantDto restaurant = frontendService.getRestaurantById(id);
            if (restaurant != null) {
                return ResponseEntity.ok(restaurant);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching restaurant {} via API: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API endpoint to get menu items for a restaurant
     */
    @GetMapping("/api/restaurants/{restaurantId}/menu")
    @ResponseBody
    public ResponseEntity<List<MenuItemDto>> getMenuItems(@PathVariable Long restaurantId) {
        try {
            logger.debug("API request: Get menu items for restaurant {}", restaurantId);
            List<MenuItemDto> menuItems = frontendService.getMenuItems(restaurantId);
            return ResponseEntity.ok(menuItems);
        } catch (Exception e) {
            logger.error("Error fetching menu items via API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API endpoint to get all orders
     */
    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<List<OrderDto>> getOrders() {
        try {
            logger.debug("API request: Get all orders");
            List<OrderDto> orders = frontendService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching orders via API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API endpoint to get order by ID
     */
    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id) {
        try {
            logger.debug("API request: Get order {}", id);
            OrderDto order = frontendService.getOrderById(id);
            if (order != null) {
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching order {} via API: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API endpoint to create a new order
     */
    @PostMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto order) {
        try {
            logger.info("API request: Create new order");
            OrderDto createdOrder = frontendService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            logger.error("Error creating order via API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * API endpoint to update order status
     */
    @PutMapping("/api/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        try {
            logger.info("API request: Update order {} status to {}", id, status);
            OrderDto updatedOrder = frontendService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            logger.error("Error updating order status via API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * API endpoint to get orders by customer ID
     */
    @GetMapping("/api/orders/customer/{customerId}")
    @ResponseBody
    public ResponseEntity<List<OrderDto>> getOrdersByCustomer(@PathVariable Long customerId) {
        try {
            logger.debug("API request: Get orders for customer {}", customerId);
            List<OrderDto> orders = frontendService.getOrdersByCustomerId(customerId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching customer orders via API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API endpoint to get orders by restaurant ID
     */
    @GetMapping("/api/orders/restaurant/{restaurantId}")
    @ResponseBody
    public ResponseEntity<List<OrderDto>> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        try {
            logger.debug("API request: Get orders for restaurant {}", restaurantId);
            List<OrderDto> orders = frontendService.getOrdersByRestaurantId(restaurantId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching restaurant orders via API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API endpoint to get system health status
     */
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        try {
            logger.debug("API request: Health check");
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("frontend", "HEALTHY");
            health.put("services", Map.of(
                "restaurant", frontendService.isServiceHealthy("restaurant"),
                "order", frontendService.isServiceHealthy("order"),
                "delivery", frontendService.isServiceHealthy("delivery"),
                "payment", frontendService.isServiceHealthy("payment")
            ));
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Error checking health via API: {}", e.getMessage());
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    /**
     * API endpoint for service discovery information
     */
    @GetMapping("/api/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "frontend-service");
        info.put("version", "1.0.0");
        info.put("description", "Food Delivery System Frontend Service");
        info.put("apiGateway", apiGatewayUrl);
        info.put("features", new String[]{
            "Modern Responsive UI",
            "Real-time Order Tracking", 
            "Advanced Payment Options",
            "Restaurant Management",
            "Order Management",
            "Admin Dashboard"
        });
        return ResponseEntity.ok(info);
    }
}
