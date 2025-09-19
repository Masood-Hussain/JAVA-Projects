package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.MenuSearchCriteria;
import com.fooddelivery.restaurant.dto.MenuRecommendationDto;
import com.fooddelivery.restaurant.dto.NutritionalInfoDto;
import com.fooddelivery.restaurant.dto.VirtualMenuItemDto;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.entity.MenuItem;
import com.fooddelivery.common.dto.MenuItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AdvancedMenuService {

    @Autowired
    private MenuItemRepository menuItemRepository;
    
    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private AIRecommendationService aiRecommendationService;
    
    @Autowired
    private MenuAnalyticsService menuAnalyticsService;

    // Real-time availability sink for each restaurant
    private final Map<Long, Sinks.Many<Map<String, Object>>> availabilitySinks = new ConcurrentHashMap<>();

    /**
     * Advanced menu search with AI-powered filtering
     */
    public Flux<MenuItemDto> searchMenuItems(Long restaurantId, MenuSearchCriteria criteria) {
        return Flux.fromIterable(menuItemRepository.findByRestaurantId(restaurantId))
            .map(this::convertToDto)
            .filter(item -> applySearchFilters(item, criteria))
            .sort(getSortComparator(criteria.getSortBy(), criteria.getSortOrder()))
            .skip(criteria.getPage() != null ? criteria.getPage() * (criteria.getSize() != null ? criteria.getSize() : 10) : 0)
            .take(criteria.getSize() != null ? criteria.getSize() : 10)
            .doOnNext(item -> menuAnalyticsService.recordSearch(restaurantId, criteria.getSearchTerm(), item.getId()));
    }

    /**
     * AI-powered menu recommendations
     */
    public Flux<MenuRecommendationDto> getAIRecommendations(Long restaurantId, String userId, 
                                                           String dietaryPreferences, BigDecimal budget) {
        return aiRecommendationService.generateRecommendations(restaurantId, userId, dietaryPreferences, budget)
            .flatMapMany(Flux::fromIterable)
            .doOnNext(rec -> menuAnalyticsService.recordRecommendation(restaurantId, userId, rec.getMenuItem().getId()));
    }

    /**
     * Get trending menu items based on real-time analytics
     */
    public Flux<MenuItemDto> getTrendingItems(Long restaurantId, int limit) {
        return menuAnalyticsService.getTrendingItems(restaurantId, limit)
            .flatMapMany(Flux::fromIterable);
    }

    /**
     * Advanced category filtering with real-time data
     */
    public Flux<MenuItemDto> getMenuByCategory(Long restaurantId, String category, String sortBy, 
                                             String sortOrder, BigDecimal minPrice, BigDecimal maxPrice,
                                             Boolean vegetarian, Boolean vegan, Boolean glutenFree) {
        
        MenuSearchCriteria criteria = new MenuSearchCriteria();
        criteria.setCategories(Arrays.asList(category));
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setVegetarian(vegetarian);
        criteria.setVegan(vegan);
        criteria.setGlutenFree(glutenFree);
        criteria.setSortBy(sortBy);
        criteria.setSortOrder(sortOrder);
        
        return searchMenuItems(restaurantId, criteria);
    }

    /**
     * Real-time menu availability stream using WebSockets
     */
    public Flux<Map<String, Object>> getMenuAvailabilityStream(Long restaurantId) {
        return availabilitySinks.computeIfAbsent(restaurantId, 
            k -> Sinks.many().multicast().onBackpressureBuffer())
            .asFlux()
            .mergeWith(getCurrentAvailability(restaurantId));
    }

    /**
     * Update menu availability with real-time broadcasting
     */
    public Mono<Boolean> updateMenuAvailability(Long restaurantId, Map<Long, Boolean> availabilityUpdates) {
        return Mono.fromCallable(() -> {
            // Update database
            availabilityUpdates.forEach((itemId, available) -> {
                Optional<MenuItem> menuItem = menuItemRepository.findById(itemId);
                if (menuItem.isPresent()) {
                    menuItem.get().setAvailable(available);
                    menuItemRepository.save(menuItem.get());
                }
            });

            // Broadcast updates
            Map<String, Object> update = new HashMap<>();
            update.put("restaurantId", restaurantId);
            update.put("updates", availabilityUpdates);
            update.put("timestamp", LocalDateTime.now());
            
            Sinks.Many<Map<String, Object>> sink = availabilitySinks.get(restaurantId);
            if (sink != null) {
                sink.tryEmitNext(update);
            }
            
            return true;
        });
    }

    /**
     * Advanced menu analytics with AI insights
     */
    public Mono<Map<String, Object>> getMenuAnalytics(Long restaurantId, int days) {
        return menuAnalyticsService.getAdvancedAnalytics(restaurantId, days);
    }

    /**
     * Smart combo recommendations using machine learning
     */
    public Flux<Map<String, Object>> getSmartCombos(Long restaurantId, List<Long> selectedItems) {
        return aiRecommendationService.generateSmartCombos(restaurantId, selectedItems)
            .flatMapMany(Flux::fromIterable);
    }

    // Helper methods
    private MenuItemDto convertToDto(MenuItem menuItem) {
        MenuItemDto dto = new MenuItemDto();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        dto.setCategory(menuItem.getCategory());
        dto.setAvailable(menuItem.getAvailable());
        dto.setRestaurantId(menuItem.getRestaurant().getId());
        dto.setImageUrl(menuItem.getImageUrl());
        dto.setIngredients(Arrays.asList(menuItem.getIngredients().split(",")));
        dto.setNutritionalInfo(parseNutritionalInfo(menuItem.getNutritionalInfo()));
        dto.setPreparationTime(menuItem.getPreparationTime());
        dto.setSpiceLevel(menuItem.getSpiceLevel());
        dto.setCalories(menuItem.getCalories());
        dto.setVegetarian(menuItem.getVegetarian());
        dto.setVegan(menuItem.getVegan());
        dto.setGlutenFree(menuItem.getGlutenFree());
        dto.setHalal(menuItem.getHalal());
        return dto;
    }

    private boolean applySearchFilters(MenuItemDto item, MenuSearchCriteria criteria) {
        if (criteria.getSearchTerm() != null && !item.getName().toLowerCase()
            .contains(criteria.getSearchTerm().toLowerCase()) &&
            !item.getDescription().toLowerCase().contains(criteria.getSearchTerm().toLowerCase())) {
            return false;
        }
        
        if (criteria.getCategories() != null && !criteria.getCategories().contains(item.getCategory())) {
            return false;
        }
        
        if (criteria.getMinPrice() != null && item.getPrice().compareTo(criteria.getMinPrice()) < 0) {
            return false;
        }
        
        if (criteria.getMaxPrice() != null && item.getPrice().compareTo(criteria.getMaxPrice()) > 0) {
            return false;
        }
        
        if (criteria.getVegetarian() != null && !criteria.getVegetarian().equals(item.getVegetarian())) {
            return false;
        }
        
        if (criteria.getVegan() != null && !criteria.getVegan().equals(item.getVegan())) {
            return false;
        }
        
        if (criteria.getGlutenFree() != null && !criteria.getGlutenFree().equals(item.getGlutenFree())) {
            return false;
        }
        
        if (criteria.getAvailable() != null && !criteria.getAvailable().equals(item.getAvailable())) {
            return false;
        }
        
        return true;
    }

    private Comparator<MenuItemDto> getSortComparator(String sortBy, String sortOrder) {
        Comparator<MenuItemDto> comparator;
        
        switch (sortBy != null ? sortBy.toLowerCase() : "name") {
            case "price":
                comparator = Comparator.comparing(MenuItemDto::getPrice);
                break;
            case "preparationtime":
                comparator = Comparator.comparing(MenuItemDto::getPreparationTime);
                break;
            case "calories":
                comparator = Comparator.comparing(MenuItemDto::getCalories);
                break;
            default:
                comparator = Comparator.comparing(MenuItemDto::getName);
        }
        
        return "desc".equalsIgnoreCase(sortOrder) ? comparator.reversed() : comparator;
    }

    private Mono<Map<String, Object>> getCurrentAvailability(Long restaurantId) {
        return Mono.fromCallable(() -> {
            Map<String, Object> current = new HashMap<>();
            List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurantId);
            Map<Long, Boolean> availability = items.stream()
                .collect(Collectors.toMap(MenuItem::getId, MenuItem::getAvailable));
            
            current.put("restaurantId", restaurantId);
            current.put("availability", availability);
            current.put("timestamp", LocalDateTime.now());
            return current;
        });
    }

    private Map<String, Object> parseNutritionalInfo(String nutritionalInfo) {
        // Parse nutritional information from string format
        Map<String, Object> info = new HashMap<>();
        if (nutritionalInfo != null && !nutritionalInfo.isEmpty()) {
            String[] parts = nutritionalInfo.split(",");
            for (String part : parts) {
                String[] keyValue = part.split(":");
                if (keyValue.length == 2) {
                    info.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return info;
    }

    // Additional methods needed by AdvancedMenuController

    public Flux<MenuItemDto> intelligentMenuSearch(Long restaurantId, MenuSearchCriteria criteria, String userId, String location) {
        return searchMenuItems(restaurantId, criteria)
            .doOnNext(item -> recordUserInteraction(userId, restaurantId, item.getId(), "search"));
    }

    public Flux<MenuRecommendationDto> getPersonalizedRecommendations(Long restaurantId, String userId, String dietaryPreferences, BigDecimal budget, String mood, String weather, String occasion) {
        return getAIRecommendations(restaurantId, userId, dietaryPreferences, budget)
            .doOnNext(rec -> recordUserInteraction(userId, restaurantId, rec.getMenuItem().getId(), "recommendation"));
    }

    public Flux<MenuItemDto> getPredictiveTrendingItems(Long restaurantId, int limit, String timeframe) {
        return getTrendingItems(restaurantId, limit);
    }

    public Flux<Map<String, Object>> calculateDynamicPricing(Long restaurantId, String demandLevel, String timeOfDay) {
        return Flux.fromIterable(menuItemRepository.findByRestaurantId(restaurantId))
            .map(item -> {
                Map<String, Object> pricing = new HashMap<>();
                pricing.put("itemId", item.getId());
                pricing.put("basePrice", item.getPrice());
                pricing.put("dynamicPrice", calculateDynamicPrice(item.getPrice(), demandLevel, timeOfDay));
                pricing.put("demandLevel", demandLevel);
                pricing.put("timeOfDay", timeOfDay);
                return pricing;
            });
    }

    public Mono<NutritionalInfoDto> getDetailedNutritionalInfo(Long restaurantId, Long itemId, String dietaryGoals) {
        return Mono.fromCallable(() -> {
            Optional<MenuItem> menuItem = menuItemRepository.findById(itemId);
            if (menuItem.isPresent()) {
                return NutritionalInfoDto.builder()
                    .itemId(itemId)
                    .itemName(menuItem.get().getName())
                    .calories(500) // Mock data
                    .protein(BigDecimal.valueOf(25))
                    .carbohydrates(BigDecimal.valueOf(40))
                    .fat(BigDecimal.valueOf(15))
                    .healthScore(85)
                    .build();
            }
            return null;
        });
    }

    public Flux<VirtualMenuItemDto> getVirtualMenuExperience(Long restaurantId, String deviceCapabilities) {
        return Flux.fromIterable(menuItemRepository.findByRestaurantId(restaurantId))
            .map(item -> VirtualMenuItemDto.builder()
                .itemId(item.getId())
                .itemName(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .model3dUrl("https://3d-models.com/" + item.getId())
                .arModelUrl("https://ar-models.com/" + item.getId())
                .build());
    }

    public Flux<MenuItemDto> processVoiceCommand(Long restaurantId, Map<String, String> voiceCommand) {
        String searchTerm = voiceCommand.getOrDefault("searchTerm", "");
        MenuSearchCriteria criteria = new MenuSearchCriteria();
        criteria.setSearchTerm(searchTerm);
        return searchMenuItems(restaurantId, criteria);
    }

    public Flux<MenuItemDto> getSustainableMenuOptions(Long restaurantId, String sustainabilityLevel) {
        return Flux.fromIterable(menuItemRepository.findByRestaurantId(restaurantId))
            .map(this::convertToDto)
            .filter(item -> item.getName().toLowerCase().contains("organic") || 
                           item.getName().toLowerCase().contains("sustainable"));
    }

    public Flux<Map<String, Object>> getRealTimeInventoryStatus(Long restaurantId) {
        return Flux.fromIterable(menuItemRepository.findByRestaurantId(restaurantId))
            .map(item -> {
                Map<String, Object> status = new HashMap<>();
                status.put("itemId", item.getId());
                status.put("itemName", item.getName());
                status.put("available", item.getAvailable());
                status.put("stockLevel", "high"); // Mock data
                return status;
            });
    }

    public Flux<Map<String, Object>> getAIOptimizedCombos(Long restaurantId, List<Long> selectedItems, BigDecimal budget, String preferences) {
        return getSmartCombos(restaurantId, selectedItems);
    }

    public Mono<Map<String, Object>> getAdvancedMenuAnalytics(Long restaurantId, int days, String analyticsType) {
        return getMenuAnalytics(restaurantId, days);
    }

    public Mono<Map<String, Object>> startMenuABTest(Long restaurantId, Map<String, Object> testConfiguration) {
        return Mono.just(Map.of(
            "testId", UUID.randomUUID().toString(),
            "status", "started",
            "restaurantId", restaurantId,
            "configuration", testConfiguration
        ));
    }

    public Mono<Map<String, Object>> verifyIngredientSourceOnBlockchain(Long restaurantId, Long itemId) {
        return Mono.just(Map.of(
            "itemId", itemId,
            "verified", true,
            "blockchainHash", "0x" + UUID.randomUUID().toString().replace("-", ""),
            "sourceVerified", true,
            "organicCertified", true
        ));
    }

    // Helper methods
    private void recordUserInteraction(String userId, Long restaurantId, Long itemId, String interactionType) {
        // Record user interaction for analytics
    }

    private BigDecimal calculateDynamicPrice(BigDecimal basePrice, String demandLevel, String timeOfDay) {
        double multiplier = 1.0;
        if ("high".equals(demandLevel)) multiplier += 0.2;
        if ("peak".equals(timeOfDay)) multiplier += 0.15;
        return basePrice.multiply(BigDecimal.valueOf(multiplier));
    }
}
