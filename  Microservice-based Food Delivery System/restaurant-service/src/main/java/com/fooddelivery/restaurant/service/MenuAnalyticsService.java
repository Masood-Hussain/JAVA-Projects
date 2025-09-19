package com.fooddelivery.restaurant.service;

import com.fooddelivery.common.dto.MenuItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MenuAnalyticsService {

    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;
    
    // In-memory analytics for demonstration (in production, use proper analytics DB)
    private final Map<String, Integer> searchCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> viewCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> orderCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAccessed = new ConcurrentHashMap<>();

    /**
     * Record menu search analytics
     */
    public void recordSearch(Long restaurantId, String searchTerm, Long menuItemId) {
        String key = String.format("search:%d:%s:%d", restaurantId, searchTerm, menuItemId);
        searchCounts.merge(key, 1, Integer::sum);
        lastAccessed.put(key, LocalDateTime.now());
    }

    /**
     * Record menu item view
     */
    public void recordView(Long restaurantId, Long menuItemId) {
        String key = String.format("view:%d:%d", restaurantId, menuItemId);
        viewCounts.merge(key, 1, Integer::sum);
        lastAccessed.put(key, LocalDateTime.now());
    }

    /**
     * Record menu item order
     */
    public void recordOrder(Long restaurantId, Long menuItemId, int quantity) {
        String key = String.format("order:%d:%d", restaurantId, menuItemId);
        orderCounts.merge(key, quantity, Integer::sum);
        lastAccessed.put(key, LocalDateTime.now());
    }

    /**
     * Record recommendation click
     */
    public void recordRecommendation(Long restaurantId, String userId, Long menuItemId) {
        String key = String.format("recommendation:%d:%s:%d", restaurantId, userId, menuItemId);
        viewCounts.merge(key, 1, Integer::sum);
        lastAccessed.put(key, LocalDateTime.now());
    }

    /**
     * Get trending menu items based on recent activity
     */
    public Mono<List<MenuItemDto>> getTrendingItems(Long restaurantId, int limit) {
        return Mono.fromCallable(() -> {
            // Calculate trending score based on recent views, orders, and searches
            Map<Long, Double> trendingScores = new HashMap<>();
            
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            
            // Weight recent activity more heavily
            viewCounts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("view:" + restaurantId))
                .filter(entry -> lastAccessed.getOrDefault(entry.getKey(), LocalDateTime.MIN).isAfter(cutoff))
                .forEach(entry -> {
                    String[] parts = entry.getKey().split(":");
                    if (parts.length >= 3) {
                        Long menuItemId = Long.parseLong(parts[2]);
                        trendingScores.merge(menuItemId, entry.getValue() * 1.0, Double::sum);
                    }
                });
            
            orderCounts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("order:" + restaurantId))
                .filter(entry -> lastAccessed.getOrDefault(entry.getKey(), LocalDateTime.MIN).isAfter(cutoff))
                .forEach(entry -> {
                    String[] parts = entry.getKey().split(":");
                    if (parts.length >= 3) {
                        Long menuItemId = Long.parseLong(parts[2]);
                        trendingScores.merge(menuItemId, entry.getValue() * 3.0, Double::sum); // Orders weighted more
                    }
                });
            
            // Convert to sorted list of trending items
            return trendingScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> createTrendingMenuItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        });
    }

    /**
     * Get popularity scores for all menu items
     */
    public Map<Long, Double> getPopularityScores(Long restaurantId) {
        Map<Long, Double> scores = new HashMap<>();
        
        // Calculate based on historical data
        viewCounts.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("view:" + restaurantId))
            .forEach(entry -> {
                String[] parts = entry.getKey().split(":");
                if (parts.length >= 3) {
                    Long menuItemId = Long.parseLong(parts[2]);
                    scores.merge(menuItemId, entry.getValue() * 0.1, Double::sum);
                }
            });
        
        orderCounts.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("order:" + restaurantId))
            .forEach(entry -> {
                String[] parts = entry.getKey().split(":");
                if (parts.length >= 3) {
                    Long menuItemId = Long.parseLong(parts[2]);
                    scores.merge(menuItemId, entry.getValue() * 0.5, Double::sum);
                }
            });
        
        // Normalize scores to 0-1 range
        double maxScore = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        scores.replaceAll((k, v) -> v / maxScore);
        
        return scores;
    }

    /**
     * Get comprehensive analytics for a restaurant
     */
    public Mono<Map<String, Object>> getAdvancedAnalytics(Long restaurantId, int days) {
        return Mono.fromCallable(() -> {
            Map<String, Object> analytics = new HashMap<>();
            
            LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
            
            // Top searched terms
            Map<String, Integer> topSearches = searchCounts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("search:" + restaurantId))
                .filter(entry -> lastAccessed.getOrDefault(entry.getKey(), LocalDateTime.MIN).isAfter(cutoff))
                .collect(Collectors.groupingBy(
                    entry -> entry.getKey().split(":")[2], // Extract search term
                    Collectors.summingInt(Map.Entry::getValue)
                ));
            
            // Most viewed items
            Map<Long, Integer> topViewed = viewCounts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("view:" + restaurantId))
                .filter(entry -> lastAccessed.getOrDefault(entry.getKey(), LocalDateTime.MIN).isAfter(cutoff))
                .collect(Collectors.groupingBy(
                    entry -> Long.parseLong(entry.getKey().split(":")[2]),
                    Collectors.summingInt(Map.Entry::getValue)
                ));
            
            // Most ordered items
            Map<Long, Integer> topOrdered = orderCounts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("order:" + restaurantId))
                .filter(entry -> lastAccessed.getOrDefault(entry.getKey(), LocalDateTime.MIN).isAfter(cutoff))
                .collect(Collectors.groupingBy(
                    entry -> Long.parseLong(entry.getKey().split(":")[2]),
                    Collectors.summingInt(Map.Entry::getValue)
                ));
            
            analytics.put("topSearchTerms", topSearches);
            analytics.put("mostViewedItems", topViewed);
            analytics.put("mostOrderedItems", topOrdered);
            analytics.put("analyticsGeneratedAt", LocalDateTime.now());
            analytics.put("periodDays", days);
            
            // Performance metrics
            int totalViews = topViewed.values().stream().mapToInt(Integer::intValue).sum();
            int totalOrders = topOrdered.values().stream().mapToInt(Integer::intValue).sum();
            double conversionRate = totalViews > 0 ? (double) totalOrders / totalViews : 0.0;
            
            analytics.put("totalViews", totalViews);
            analytics.put("totalOrders", totalOrders);
            analytics.put("conversionRate", String.format("%.2f%%", conversionRate * 100));
            
            return analytics;
        });
    }

    private MenuItemDto createTrendingMenuItem(Long menuItemId, Double trendingScore) {
        // In a real implementation, fetch from database
        MenuItemDto dto = new MenuItemDto();
        dto.setId(menuItemId);
        dto.setName("Trending Item " + menuItemId);
        dto.setDescription("This item is trending with score: " + String.format("%.2f", trendingScore));
        // Add other properties as needed
        return dto;
    }
}
