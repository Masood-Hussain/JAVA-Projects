package com.fooddelivery.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/restaurant")
    public ResponseEntity<Map<String, Object>> restaurantFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Restaurant service is temporarily unavailable. Please try again later.");
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("timestamp", LocalDateTime.now());
        response.put("fallback", true);
        response.put("suggestion", "You can browse cached menus or try basic search functionality.");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payment service is temporarily unavailable. Your order is saved and will be processed shortly.");
        response.put("status", "PAYMENT_DELAYED");
        response.put("timestamp", LocalDateTime.now());
        response.put("fallback", true);
        response.put("suggestion", "Try alternative payment methods or check back in a few minutes.");
        response.put("orderId", "Will be provided when service is restored");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order service is temporarily unavailable. Your request is queued for processing.");
        response.put("status", "ORDER_QUEUED");
        response.put("timestamp", LocalDateTime.now());
        response.put("fallback", true);
        response.put("suggestion", "You will receive a confirmation email once the service is restored.");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/delivery")
    public ResponseEntity<Map<String, Object>> deliveryFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Delivery tracking is temporarily unavailable. Your order is still being processed.");
        response.put("status", "TRACKING_UNAVAILABLE");
        response.put("timestamp", LocalDateTime.now());
        response.put("fallback", true);
        response.put("suggestion", "Contact customer support for delivery updates or check SMS notifications.");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/ai")
    public ResponseEntity<Map<String, Object>> aiFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "AI recommendation service is temporarily unavailable.");
        response.put("status", "AI_UNAVAILABLE");
        response.put("timestamp", LocalDateTime.now());
        response.put("fallback", true);
        response.put("suggestion", "Browse featured items or use category-based search instead.");
        response.put("basicRecommendations", "Popular items from your favorite cuisines are still available.");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
