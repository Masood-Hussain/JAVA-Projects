package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.service.AdvancedMenuService;
import com.fooddelivery.restaurant.dto.MenuSearchCriteria;
import com.fooddelivery.restaurant.dto.MenuRecommendationDto;
import com.fooddelivery.common.dto.MenuItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/restaurants")
@CrossOrigin(origins = "*")
public class AdvancedMenuController {

    @Autowired
    private AdvancedMenuService advancedMenuService;

    /**
     * Advanced menu search with filters, sorting, and AI recommendations
     */
    @PostMapping("/{restaurantId}/menu/search")
    public Flux<MenuItemDto> searchMenuItems(
            @PathVariable Long restaurantId,
            @RequestBody MenuSearchCriteria criteria) {
        return advancedMenuService.searchMenuItems(restaurantId, criteria);
    }

    /**
     * Get AI-powered menu recommendations based on user preferences
     */
    @GetMapping("/{restaurantId}/menu/recommendations")
    public Flux<MenuRecommendationDto> getMenuRecommendations(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String dietaryPreferences,
            @RequestParam(required = false) BigDecimal budget) {
        return advancedMenuService.getAIRecommendations(restaurantId, userId, dietaryPreferences, budget);
    }

    /**
     * Get trending menu items based on real-time analytics
     */
    @GetMapping("/{restaurantId}/menu/trending")
    public Flux<MenuItemDto> getTrendingItems(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "10") int limit) {
        return advancedMenuService.getTrendingItems(restaurantId, limit);
    }

    /**
     * Get menu items by category with advanced filtering
     */
    @GetMapping("/{restaurantId}/menu/category/{category}")
    public Flux<MenuItemDto> getMenuByCategory(
            @PathVariable Long restaurantId,
            @PathVariable String category,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) Boolean vegan,
            @RequestParam(required = false) Boolean glutenFree) {
        return advancedMenuService.getMenuByCategory(
            restaurantId, category, sortBy, sortOrder, 
            minPrice, maxPrice, vegetarian, vegan, glutenFree);
    }

    /**
     * Real-time menu availability updates
     */
    @GetMapping("/{restaurantId}/menu/availability")
    public Flux<Map<String, Object>> getMenuAvailabilityUpdates(@PathVariable Long restaurantId) {
        return advancedMenuService.getMenuAvailabilityStream(restaurantId);
    }

    /**
     * Bulk update menu item availability
     */
    @PutMapping("/{restaurantId}/menu/availability")
    public Mono<ResponseEntity<String>> updateMenuAvailability(
            @PathVariable Long restaurantId,
            @RequestBody Map<Long, Boolean> availabilityUpdates) {
        return advancedMenuService.updateMenuAvailability(restaurantId, availabilityUpdates)
            .map(success -> ResponseEntity.ok("Menu availability updated successfully"))
            .onErrorReturn(ResponseEntity.badRequest().body("Failed to update menu availability"));
    }

    /**
     * Get menu analytics and insights
     */
    @GetMapping("/{restaurantId}/menu/analytics")
    public Mono<ResponseEntity<Map<String, Object>>> getMenuAnalytics(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "30") int days) {
        return advancedMenuService.getMenuAnalytics(restaurantId, days)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Smart combo recommendations
     */
    @GetMapping("/{restaurantId}/menu/combos")
    public Flux<Map<String, Object>> getSmartCombos(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) List<Long> selectedItems) {
        return advancedMenuService.getSmartCombos(restaurantId, selectedItems);
    }
}
