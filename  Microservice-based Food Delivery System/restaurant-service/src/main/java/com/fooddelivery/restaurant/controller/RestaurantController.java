package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.service.RestaurantService;
import com.fooddelivery.restaurant.service.MenuItemService;
import com.fooddelivery.common.dto.RestaurantDto;
import com.fooddelivery.common.dto.MenuItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {
    private static final Logger log = LoggerFactory.getLogger(RestaurantController.class);
    
    @Autowired
    private RestaurantService restaurantService;
    
    @Autowired
    private MenuItemService menuItemService;
    
    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
        List<RestaurantDto> restaurants = restaurantService.getAllActiveRestaurants();
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable Long id) {
        Optional<RestaurantDto> restaurant = restaurantService.getRestaurantById(id);
        return restaurant.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<RestaurantDto> createRestaurant(@Valid @RequestBody RestaurantDto restaurantDto) {
        RestaurantDto created = restaurantService.createRestaurant(restaurantDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDto> updateRestaurant(@PathVariable Long id, 
                                                         @Valid @RequestBody RestaurantDto restaurantDto) {
        try {
            RestaurantDto updated = restaurantService.updateRestaurant(id, restaurantDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/cuisine/{cuisine}")
    public ResponseEntity<List<RestaurantDto>> getRestaurantsByCuisine(@PathVariable String cuisine) {
        List<RestaurantDto> restaurants = restaurantService.getRestaurantsByCuisine(cuisine);
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<RestaurantDto>> searchRestaurants(@RequestParam String name) {
        List<RestaurantDto> restaurants = restaurantService.searchRestaurantsByName(name);
        return ResponseEntity.ok(restaurants);
    }
    
    // Menu Item endpoints
    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<MenuItemDto>> getMenuItems(@PathVariable Long restaurantId) {
        List<MenuItemDto> menuItems = menuItemService.getAvailableMenuItemsByRestaurant(restaurantId);
        return ResponseEntity.ok(menuItems);
    }
    
    @PostMapping("/{restaurantId}/menu")
    public ResponseEntity<MenuItemDto> addMenuItem(@PathVariable Long restaurantId, 
                                                  @Valid @RequestBody MenuItemDto menuItemDto) {
        menuItemDto.setRestaurantId(restaurantId);
        try {
            MenuItemDto created = menuItemService.createMenuItem(menuItemDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Failed to create menu item for restaurant {}: {}", restaurantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{restaurantId}/menu/{menuItemId}")
    public ResponseEntity<MenuItemDto> updateMenuItem(@PathVariable Long restaurantId,
                                                     @PathVariable Long menuItemId, 
                                                     @Valid @RequestBody MenuItemDto menuItemDto) {
        menuItemDto.setRestaurantId(restaurantId);
        Optional<MenuItemDto> updated = menuItemService.updateMenuItem(menuItemId, menuItemDto);
        return updated.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{restaurantId}/menu/{menuItemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long restaurantId,
                                              @PathVariable Long menuItemId) {
        boolean deleted = menuItemService.deleteMenuItem(menuItemId);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
}
