package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.MenuRecommendationDto;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.entity.MenuItem;
import com.fooddelivery.common.dto.MenuItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {

    @Autowired
    private MenuItemRepository menuItemRepository;
    
    @Autowired
    private MenuAnalyticsService menuAnalyticsService;
    
    private final WebClient aiServiceClient;
    
    public AIRecommendationService() {
        this.aiServiceClient = WebClient.builder()
            .baseUrl("http://ai-service:8080") // External AI service for advanced recommendations
            .build();
    }

    /**
     * Generate AI-powered menu recommendations
     */
    public Mono<List<MenuRecommendationDto>> generateRecommendations(Long restaurantId, String userId, 
                                                                   String dietaryPreferences, BigDecimal budget) {
        return Mono.fromCallable(() -> {
            List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(restaurantId);
            List<MenuRecommendationDto> recommendations = new ArrayList<>();
            
            // AI Algorithm for recommendations
            Map<String, Double> userPreferences = parseUserPreferences(userId, dietaryPreferences);
            Map<Long, Double> popularityScores = menuAnalyticsService.getPopularityScores(restaurantId);
            
            for (MenuItem item : menuItems) {
                if (!item.getAvailable()) continue;
                
                if (budget != null && item.getPrice().compareTo(budget) > 0) continue;
                
                double score = calculateRecommendationScore(item, userPreferences, popularityScores);
                
                if (score > 0.6) { // Threshold for recommendations
                    MenuRecommendationDto recommendation = new MenuRecommendationDto();
                    recommendation.setMenuItem(convertToDto(item));
                    recommendation.setRecommendationScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP));
                    recommendation.setRecommendationReason(generateRecommendationReason(item, userPreferences, score));
                    recommendation.setAiConfidence(getConfidenceLevel(score));
                    recommendation.setTags(generateRecommendationTags(item, userPreferences));
                    recommendation.setIsPersonalized(userId != null);
                    recommendation.setMatchingCriteria(getMatchingCriteria(item, userPreferences));
                    
                    recommendations.add(recommendation);
                }
            }
            
            // Sort by recommendation score
            recommendations.sort((a, b) -> b.getRecommendationScore().compareTo(a.getRecommendationScore()));
            
            return recommendations.stream().limit(10).collect(Collectors.toList());
        });
    }

    /**
     * Generate smart combo recommendations using ML algorithms
     */
    public Mono<List<Map<String, Object>>> generateSmartCombos(Long restaurantId, List<Long> selectedItems) {
        return Mono.fromCallable(() -> {
            List<Map<String, Object>> combos = new ArrayList<>();
            List<MenuItem> availableItems = menuItemRepository.findByRestaurantId(restaurantId)
                .stream().filter(MenuItem::getAvailable).collect(Collectors.toList());
            
            // Pattern analysis for frequent item combinations
            Map<String, List<MenuItem>> categoryGroups = availableItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory));
            
            // Generate balanced combo suggestions
            if (selectedItems != null && !selectedItems.isEmpty()) {
                List<MenuItem> selected = selectedItems.stream()
                    .map(menuItemRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
                
                combos.addAll(generateComplementaryCombos(selected, availableItems));
            } else {
                combos.addAll(generatePopularCombos(categoryGroups));
            }
            
            return combos;
        });
    }

    private double calculateRecommendationScore(MenuItem item, Map<String, Double> userPreferences, 
                                              Map<Long, Double> popularityScores) {
        double score = 0.0;
        
        // Base popularity score (40% weight)
        score += popularityScores.getOrDefault(item.getId(), 0.5) * 0.4;
        
        // Dietary preferences matching (30% weight)
        if (userPreferences.containsKey("vegetarian") && item.getVegetarian()) score += 0.3;
        if (userPreferences.containsKey("vegan") && item.getVegan()) score += 0.3;
        if (userPreferences.containsKey("glutenFree") && item.getGlutenFree()) score += 0.3;
        if (userPreferences.containsKey("halal") && item.getHalal()) score += 0.3;
        
        // Price attractiveness (15% weight)
        double priceScore = Math.max(0, (50.0 - item.getPrice().doubleValue()) / 50.0);
        score += priceScore * 0.15;
        
        // Nutritional preferences (15% weight)
        if (userPreferences.containsKey("lowCalorie") && item.getCalories() != null && item.getCalories() < 500) {
            score += 0.15;
        }
        if (userPreferences.containsKey("highProtein") && item.getNutritionalInfo() != null && 
            item.getNutritionalInfo().contains("protein")) {
            score += 0.15;
        }
        
        return Math.min(1.0, score);
    }

    private String generateRecommendationReason(MenuItem item, Map<String, Double> userPreferences, double score) {
        List<String> reasons = new ArrayList<>();
        
        if (score > 0.8) reasons.add("Highly popular choice");
        if (userPreferences.containsKey("vegetarian") && item.getVegetarian()) reasons.add("Matches your vegetarian preference");
        if (userPreferences.containsKey("vegan") && item.getVegan()) reasons.add("Perfect for vegan diet");
        if (userPreferences.containsKey("glutenFree") && item.getGlutenFree()) reasons.add("Gluten-free option");
        if (item.getPrice().compareTo(BigDecimal.valueOf(20)) < 0) reasons.add("Great value for money");
        if (item.getCalories() != null && item.getCalories() < 400) reasons.add("Light and healthy option");
        
        return reasons.isEmpty() ? "Recommended based on your taste profile" : 
               String.join(", ", reasons);
    }

    private String getConfidenceLevel(double score) {
        if (score >= 0.9) return "Very High";
        if (score >= 0.8) return "High";
        if (score >= 0.7) return "Medium";
        return "Low";
    }

    private String[] generateRecommendationTags(MenuItem item, Map<String, Double> userPreferences) {
        List<String> tags = new ArrayList<>();
        
        if (item.getVegetarian()) tags.add("Vegetarian");
        if (item.getVegan()) tags.add("Vegan");
        if (item.getGlutenFree()) tags.add("Gluten-Free");
        if (item.getHalal()) tags.add("Halal");
        if (item.getPrice().compareTo(BigDecimal.valueOf(15)) < 0) tags.add("Budget-Friendly");
        if (item.getCalories() != null && item.getCalories() < 400) tags.add("Low-Calorie");
        if (item.getPreparationTime() != null && item.getPreparationTime() < 15) tags.add("Quick-Prep");
        if (item.getSpiceLevel() != null) tags.add("Spice Level: " + item.getSpiceLevel());
        
        return tags.toArray(new String[0]);
    }

    private String getMatchingCriteria(MenuItem item, Map<String, Double> userPreferences) {
        List<String> criteria = new ArrayList<>();
        
        userPreferences.keySet().forEach(pref -> {
            switch (pref) {
                case "vegetarian": if (item.getVegetarian()) criteria.add("Vegetarian"); break;
                case "vegan": if (item.getVegan()) criteria.add("Vegan"); break;
                case "glutenFree": if (item.getGlutenFree()) criteria.add("Gluten-Free"); break;
                case "halal": if (item.getHalal()) criteria.add("Halal"); break;
                case "lowCalorie": if (item.getCalories() != null && item.getCalories() < 500) criteria.add("Low-Calorie"); break;
            }
        });
        
        return String.join(", ", criteria);
    }

    private Map<String, Double> parseUserPreferences(String userId, String dietaryPreferences) {
        Map<String, Double> preferences = new HashMap<>();
        
        if (dietaryPreferences != null) {
            String[] prefs = dietaryPreferences.toLowerCase().split(",");
            for (String pref : prefs) {
                preferences.put(pref.trim(), 1.0);
            }
        }
        
        // If userId is provided, could fetch from user profile service
        // For now, using basic preferences
        
        return preferences;
    }

    private List<Map<String, Object>> generateComplementaryCombos(List<MenuItem> selected, List<MenuItem> available) {
        List<Map<String, Object>> combos = new ArrayList<>();
        
        // Logic to suggest complementary items
        Set<String> selectedCategories = selected.stream()
            .map(MenuItem::getCategory)
            .collect(Collectors.toSet());
        
        // Suggest items from different categories
        List<MenuItem> complementary = available.stream()
            .filter(item -> !selectedCategories.contains(item.getCategory()))
            .filter(item -> selected.stream().noneMatch(s -> s.getId().equals(item.getId())))
            .limit(5)
            .collect(Collectors.toList());
        
        for (MenuItem item : complementary) {
            Map<String, Object> combo = new HashMap<>();
            combo.put("item", convertToDto(item));
            combo.put("reason", "Complements your selection");
            combo.put("discount", calculateComboDiscount(selected, item));
            combos.add(combo);
        }
        
        return combos;
    }

    private List<Map<String, Object>> generatePopularCombos(Map<String, List<MenuItem>> categoryGroups) {
        List<Map<String, Object>> combos = new ArrayList<>();
        
        // Generate popular combinations across categories
        if (categoryGroups.containsKey("main") && categoryGroups.containsKey("beverage")) {
            MenuItem main = categoryGroups.get("main").get(0);
            MenuItem beverage = categoryGroups.get("beverage").get(0);
            
            Map<String, Object> combo = new HashMap<>();
            combo.put("items", Arrays.asList(convertToDto(main), convertToDto(beverage)));
            combo.put("title", "Popular Main + Drink Combo");
            combo.put("totalPrice", main.getPrice().add(beverage.getPrice()));
            combo.put("discount", "10%");
            combos.add(combo);
        }
        
        return combos;
    }

    private String calculateComboDiscount(List<MenuItem> selected, MenuItem additional) {
        // Simple discount calculation based on combo size
        if (selected.size() >= 2) return "15%";
        if (selected.size() == 1) return "10%";
        return "5%";
    }

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
        dto.setIngredients(menuItem.getIngredients() != null ? 
            Arrays.asList(menuItem.getIngredients().split(",")) : new ArrayList<>());
        dto.setPreparationTime(menuItem.getPreparationTime());
        dto.setSpiceLevel(menuItem.getSpiceLevel());
        dto.setCalories(menuItem.getCalories());
        dto.setVegetarian(menuItem.getVegetarian());
        dto.setVegan(menuItem.getVegan());
        dto.setGlutenFree(menuItem.getGlutenFree());
        dto.setHalal(menuItem.getHalal());
        return dto;
    }
}
