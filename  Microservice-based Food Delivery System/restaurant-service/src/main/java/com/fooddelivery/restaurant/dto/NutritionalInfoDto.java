package com.fooddelivery.restaurant.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NutritionalInfoDto {
    private Long itemId;
    private String itemName;
    private Integer calories;
    private BigDecimal protein; // grams
    private BigDecimal carbohydrates; // grams
    private BigDecimal fat; // grams
    private BigDecimal fiber; // grams
    private BigDecimal sugar; // grams
    private BigDecimal sodium; // mg
    private BigDecimal cholesterol; // mg
    
    // Vitamins and minerals
    private Map<String, BigDecimal> vitamins;
    private Map<String, BigDecimal> minerals;
    
    // Dietary information
    private List<String> allergens;
    private List<String> dietaryRestrictions; // vegetarian, vegan, keto, etc.
    private Boolean isGlutenFree;
    private Boolean isLactoseFree;
    private Boolean isNutFree;
    
    // Health scoring
    private Integer healthScore; // 1-100
    private String healthGrade; // A+, A, B+, B, C+, C, D, F
    private List<String> healthBenefits;
    private List<String> healthConcerns;
    
    // Sustainability
    private BigDecimal carbonFootprint; // kg CO2 equivalent
    private String sustainabilityRating; // A-F
    private Boolean isLocallySourced;
    private Boolean isOrganic;
    private String sourceLocation;
    
    // AI insights
    private String personalizedHealthTips;
    private BigDecimal fitnessGoalAlignment; // 0-1 score
    private List<String> substitutionSuggestions;
    
    private LocalDateTime lastUpdated;
}