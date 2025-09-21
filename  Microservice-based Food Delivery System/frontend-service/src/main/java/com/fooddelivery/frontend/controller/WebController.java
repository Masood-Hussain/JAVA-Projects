package com.fooddelivery.frontend.controller;

import com.fooddelivery.common.dto.*;
import com.fooddelivery.frontend.service.FrontendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebController {

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

    // Order API endpoint for frontend
    @PostMapping("/api/orders")
    @ResponseBody
    public OrderDto placeOrder(@RequestBody OrderDto order) {
        try {
            return frontendService.createOrder(order);
        } catch (Exception e) {
            throw new RuntimeException("Could not place order: " + e.getMessage());
        }
    }
}
