package com.fooddelivery.frontend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ModernUIController {

    @Value("${api.gateway.url:http://localhost:8080}")
    private String apiGatewayUrl;

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
}
