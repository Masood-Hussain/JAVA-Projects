package com.fooddelivery.frontend.controller;

import com.fooddelivery.common.dto.*;
import com.fooddelivery.common.enums.OrderStatus;
import com.fooddelivery.frontend.service.FrontendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Enhanced Web Controller for handling web pages and form submissions
 */
@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private FrontendService frontendService;

    @GetMapping("/classic")
    public String home(Model model) {
        try {
            List<RestaurantDto> restaurants = frontendService.getAllRestaurants();
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load restaurants: " + e.getMessage());
        }
        return "index";
    }

    @GetMapping("/classic/restaurants")
    public String restaurants(Model model) {
        try {
            List<RestaurantDto> restaurants = frontendService.getAllRestaurants();
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load restaurants: " + e.getMessage());
        }
        return "restaurants";
    }

    @GetMapping("/classic/restaurant/{id}")
    public String restaurantDetails(@PathVariable Long id, Model model) {
        try {
            RestaurantDto restaurant = frontendService.getRestaurantById(id);
            List<MenuItemDto> menuItems = frontendService.getMenuItems(id);
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("menuItems", menuItems);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load restaurant details: " + e.getMessage());
        }
        return "restaurant";
    }

    @GetMapping("/order")
    public String orderForm(Model model) {
        model.addAttribute("order", new OrderDto());
        return "order-form";
    }

    @PostMapping("/order")
    public String submitOrder(@ModelAttribute OrderDto order, Model model) {
        try {
            OrderDto createdOrder = frontendService.createOrder(order);
            model.addAttribute("order", createdOrder);
            return "order-confirmation";
        } catch (Exception e) {
            model.addAttribute("error", "Could not create order: " + e.getMessage());
            return "order-form";
        }
    }

    @GetMapping("/classic/orders")
    public String orders(Model model) {
        try {
            List<OrderDto> orders = frontendService.getAllOrders();
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load orders: " + e.getMessage());
        }
        return "orders";
    }

    @GetMapping("/order/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        try {
            OrderDto order = frontendService.getOrderById(id);
            model.addAttribute("order", order);
            
            // Get delivery info if available
            try {
                List<DeliveryDto> deliveries = frontendService.getAllDeliveries();
                DeliveryDto delivery = deliveries.stream()
                    .filter(d -> d.getOrderId().equals(id))
                    .findFirst()
                    .orElse(null);
                model.addAttribute("delivery", delivery);
            } catch (Exception e) {
                model.addAttribute("deliveryError", "Could not load delivery info");
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Could not load order details: " + e.getMessage());
        }
        return "order-details";
    }

    @GetMapping("/classic/admin")
    public String admin(Model model) {
        return "admin";
    }

    @GetMapping("/admin/restaurants")
    public String adminRestaurants(Model model) {
        try {
            List<RestaurantDto> restaurants = frontendService.getAllRestaurants();
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("newRestaurant", new RestaurantDto());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load restaurants: " + e.getMessage());
        }
        return "admin-restaurants";
    }

    @PostMapping("/admin/restaurants")
    public String createRestaurant(@ModelAttribute RestaurantDto restaurant, RedirectAttributes redirectAttributes) {
        try {
            RestaurantDto created = frontendService.createRestaurant(restaurant);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Restaurant '" + created.getName() + "' added successfully!");
            return "redirect:/admin/restaurants";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not create restaurant: " + e.getMessage());
            return "redirect:/admin/restaurants";
        }
    }

    @GetMapping("/admin/restaurants/{id}/edit")
    public String editRestaurant(@PathVariable Long id, Model model) {
        try {
            RestaurantDto restaurant = frontendService.getRestaurantById(id);
            model.addAttribute("restaurant", restaurant);
            return "admin-restaurant-edit";
        } catch (Exception e) {
            model.addAttribute("error", "Could not load restaurant: " + e.getMessage());
            return "redirect:/admin/restaurants";
        }
    }

    @PostMapping("/admin/restaurants/{id}/edit")
    public String updateRestaurant(@PathVariable Long id, @ModelAttribute RestaurantDto restaurant, 
                                 RedirectAttributes redirectAttributes) {
        try {
            restaurant.setId(id);
            RestaurantDto updated = frontendService.updateRestaurant(restaurant);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Restaurant '" + updated.getName() + "' updated successfully!");
            return "redirect:/admin/restaurants";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not update restaurant: " + e.getMessage());
            return "redirect:/admin/restaurants";
        }
    }

    @DeleteMapping("/admin/restaurants/{id}")
    @ResponseBody
    public String deleteRestaurant(@PathVariable Long id) {
        try {
            frontendService.deleteRestaurant(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        try {
            List<OrderDto> orders = frontendService.getAllOrders();
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load orders: " + e.getMessage());
        }
        return "admin-orders";
    }

    @GetMapping("/admin/deliveries")
    public String adminDeliveries(Model model) {
        try {
            List<DeliveryDto> deliveries = frontendService.getAllDeliveries();
            model.addAttribute("deliveries", deliveries);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load deliveries: " + e.getMessage());
        }
        return "admin-deliveries";
    }

    // Menu Item Management Endpoints
    @GetMapping("/admin/restaurants/{restaurantId}/menu")
    public String getRestaurantMenu(@PathVariable Long restaurantId, Model model) {
        try {
            RestaurantDto restaurant = frontendService.getRestaurant(restaurantId);
            List<MenuItemDto> menuItems = frontendService.getMenuItems(restaurantId);
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("menuItems", menuItems);
            model.addAttribute("restaurantId", restaurantId);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Could not load menu: " + e.getMessage());
            // Provide safe defaults so template renders without 500
            model.addAttribute("menuItems", List.of());
            model.addAttribute("restaurantId", restaurantId);
        }
        return "admin-restaurant-menu";
    }

    @GetMapping("/admin/restaurants/{restaurantId}/menu/api")
    @ResponseBody
    public List<MenuItemDto> getMenuItemsApi(@PathVariable Long restaurantId) {
        try {
            return frontendService.getMenuItems(restaurantId);
        } catch (Exception e) {
            return List.of();
        }
    }

    @PostMapping("/admin/restaurants/{restaurantId}/menu")
    public String addMenuItem(@PathVariable Long restaurantId, 
                            @ModelAttribute MenuItemDto menuItem,
                            RedirectAttributes redirectAttributes) {
        try {
            MenuItemDto created = frontendService.createMenuItem(restaurantId, menuItem);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Menu item '" + created.getName() + "' added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to add menu item: " + e.getMessage());
        }
        return "redirect:/admin/restaurants/" + restaurantId + "/menu";
    }

    @PostMapping("/admin/restaurants/{restaurantId}/menu/{itemId}")
    public String updateMenuItem(@PathVariable Long restaurantId,
                               @PathVariable Long itemId,
                               @ModelAttribute MenuItemDto menuItem,
                               RedirectAttributes redirectAttributes) {
        try {
            MenuItemDto updated = frontendService.updateMenuItem(restaurantId, itemId, menuItem);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Menu item '" + updated.getName() + "' updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to update menu item: " + e.getMessage());
        }
        return "redirect:/admin/restaurants/" + restaurantId + "/menu";
    }

    @DeleteMapping("/admin/restaurants/{restaurantId}/menu/{itemId}")
    @ResponseBody
    public String deleteMenuItem(@PathVariable Long restaurantId, @PathVariable Long itemId) {
        try {
            frontendService.deleteMenuItem(restaurantId, itemId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    // ==================== ENHANCED ORDER MANAGEMENT ====================

    /**
     * Enhanced order submission with comprehensive error handling
     */
    @PostMapping("/api/orders")
    @ResponseBody
    public OrderDto placeOrder(@RequestBody OrderDto order) {
        try {
            logger.info("Placing order for customer: {}", order.getCustomerId());
            OrderDto createdOrder = frontendService.createOrder(order);
            logger.info("Successfully placed order with ID: {}", createdOrder.getId());
            return createdOrder;
        } catch (Exception e) {
            logger.error("Error placing order: {}", e.getMessage());
            throw new RuntimeException("Could not place order: " + e.getMessage());
        }
    }

    /**
     * Update order status with logging
     */
    @PutMapping("/admin/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status, 
                                   RedirectAttributes redirectAttributes) {
        try {
            logger.info("Updating order {} status to {}", id, status);
            OrderDto updatedOrder = frontendService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Order #" + id + " status updated to " + status);
            logger.info("Successfully updated order {} status to {}", id, status);
        } catch (Exception e) {
            logger.error("Error updating order status: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Could not update order status: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    /**
     * Cancel order with reason
     */
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, @RequestParam(required = false) String reason,
                             RedirectAttributes redirectAttributes) {
        try {
            logger.info("Cancelling order {} with reason: {}", id, reason);
            OrderDto cancelledOrder = frontendService.cancelOrder(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Order #" + id + " has been cancelled successfully");
            logger.info("Successfully cancelled order: {}", id);
        } catch (Exception e) {
            logger.error("Error cancelling order: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Could not cancel order: " + e.getMessage());
        }
        return "redirect:/orders";
    }

    /**
     * Get orders by customer for profile page
     */
    @GetMapping("/profile/orders/{customerId}")
    public String customerOrders(@PathVariable Long customerId, Model model) {
        try {
            logger.debug("Fetching orders for customer: {}", customerId);
            List<OrderDto> orders = frontendService.getOrdersByCustomerId(customerId);
            model.addAttribute("orders", orders);
            model.addAttribute("customerId", customerId);
            logger.info("Successfully fetched {} orders for customer {}", orders.size(), customerId);
        } catch (Exception e) {
            logger.error("Error fetching customer orders: {}", e.getMessage());
            model.addAttribute("error", "Could not load your orders: " + e.getMessage());
        }
        return "customer-orders";
    }

    /**
     * Get orders by restaurant for restaurant dashboard
     */
    @GetMapping("/restaurant/{restaurantId}/orders")
    public String restaurantOrders(@PathVariable Long restaurantId, Model model) {
        try {
            logger.debug("Fetching orders for restaurant: {}", restaurantId);
            List<OrderDto> orders = frontendService.getOrdersByRestaurantId(restaurantId);
            RestaurantDto restaurant = frontendService.getRestaurantById(restaurantId);
            model.addAttribute("orders", orders);
            model.addAttribute("restaurant", restaurant);
            logger.info("Successfully fetched {} orders for restaurant {}", orders.size(), restaurantId);
        } catch (Exception e) {
            logger.error("Error fetching restaurant orders: {}", e.getMessage());
            model.addAttribute("error", "Could not load restaurant orders: " + e.getMessage());
        }
        return "restaurant-orders";
    }

    /**
     * Enhanced order tracking page
     */
    @GetMapping("/track/{orderId}")
    public String trackOrder(@PathVariable Long orderId, Model model) {
        try {
            logger.debug("Tracking order: {}", orderId);
            OrderDto order = frontendService.getOrderById(orderId);
            if (order != null) {
                // Get delivery information
                List<DeliveryDto> deliveries = frontendService.getDeliveriesByOrderId(orderId);
                DeliveryDto delivery = deliveries.isEmpty() ? null : deliveries.get(0);
                
                // Get payment information
                List<PaymentDto> payments = frontendService.getPaymentsByOrderId(orderId);
                
                model.addAttribute("order", order);
                model.addAttribute("delivery", delivery);
                model.addAttribute("payments", payments);
                logger.info("Successfully loaded tracking info for order: {}", orderId);
            } else {
                logger.warn("Order not found: {}", orderId);
                model.addAttribute("error", "Order not found");
            }
        } catch (Exception e) {
            logger.error("Error tracking order: {}", e.getMessage());
            model.addAttribute("error", "Could not load order tracking: " + e.getMessage());
        }
        return "order-tracking";
    }

    // ==================== PAYMENT MANAGEMENT ====================

    /**
     * Process payment for order
     */
    @PostMapping("/orders/{orderId}/payment")
    public String processPayment(@PathVariable Long orderId, @ModelAttribute PaymentDto payment,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Processing payment for order: {}", orderId);
            payment.setOrderId(orderId);
            PaymentDto processedPayment = frontendService.processPayment(payment);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Payment processed successfully. Payment ID: " + processedPayment.getId());
            logger.info("Successfully processed payment for order: {}", orderId);
            return "redirect:/track/" + orderId;
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Payment failed: " + e.getMessage());
            return "redirect:/checkout/" + orderId;
        }
    }

    /**
     * Show payment form
     */
    @GetMapping("/checkout/{orderId}")
    public String showCheckout(@PathVariable Long orderId, Model model) {
        try {
            logger.debug("Loading checkout for order: {}", orderId);
            OrderDto order = frontendService.getOrderById(orderId);
            if (order != null) {
                model.addAttribute("order", order);
                model.addAttribute("payment", new PaymentDto());
                logger.info("Successfully loaded checkout for order: {}", orderId);
            } else {
                logger.warn("Order not found for checkout: {}", orderId);
                model.addAttribute("error", "Order not found");
            }
        } catch (Exception e) {
            logger.error("Error loading checkout: {}", e.getMessage());
            model.addAttribute("error", "Could not load checkout: " + e.getMessage());
        }
        return "checkout";
    }

    // ==================== ERROR HANDLING ====================

    /**
     * Global error handler for web requests
     */
    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        logger.error("Unhandled error in web controller: {}", e.getMessage(), e);
        model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
        return "error";
    }
}
