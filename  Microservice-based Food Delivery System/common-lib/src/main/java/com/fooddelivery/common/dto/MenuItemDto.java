package com.fooddelivery.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MenuItemDto {
    private Long id;
    
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    @Positive
    private BigDecimal price;
    
    private Long restaurantId;
    
    private String category;
    private Boolean isAvailable;
    
    // Advanced 2025 features
    private String imageUrl;
    private List<String> ingredients;
    private Map<String, Object> nutritionalInfo;
    private Integer preparationTime; // in minutes
    private String spiceLevel; // MILD, MEDIUM, HOT, EXTRA_HOT
    private Integer calories;
    private Boolean vegetarian;
    private Boolean vegan;
    private Boolean glutenFree;
    private Boolean halal;
    private Boolean keto;
    private List<String> allergens;
    private BigDecimal rating;
    private Integer reviewCount;
    private String cuisine;
    private Map<String, Object> customizations;
    private Boolean featured;
    private String promotionalOffer;

    // Constructors
    public MenuItemDto() {
        this.isAvailable = true;
    }

    public MenuItemDto(Long id, String name, String description, BigDecimal price, 
                      Long restaurantId, String category, Boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.restaurantId = restaurantId;
        this.category = category;
        this.isAvailable = isAvailable != null ? isAvailable : true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    
    public Boolean getAvailable() { return isAvailable; }
    public void setAvailable(Boolean available) { this.isAvailable = available; }
    
    // Advanced feature getters and setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    
    public Map<String, Object> getNutritionalInfo() { return nutritionalInfo; }
    public void setNutritionalInfo(Map<String, Object> nutritionalInfo) { this.nutritionalInfo = nutritionalInfo; }
    
    public Integer getPreparationTime() { return preparationTime; }
    public void setPreparationTime(Integer preparationTime) { this.preparationTime = preparationTime; }
    
    public String getSpiceLevel() { return spiceLevel; }
    public void setSpiceLevel(String spiceLevel) { this.spiceLevel = spiceLevel; }
    
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    
    public Boolean getVegetarian() { return vegetarian; }
    public void setVegetarian(Boolean vegetarian) { this.vegetarian = vegetarian; }
    
    public Boolean getVegan() { return vegan; }
    public void setVegan(Boolean vegan) { this.vegan = vegan; }
    
    public Boolean getGlutenFree() { return glutenFree; }
    public void setGlutenFree(Boolean glutenFree) { this.glutenFree = glutenFree; }
    
    public Boolean getHalal() { return halal; }
    public void setHalal(Boolean halal) { this.halal = halal; }
    
    public Boolean getKeto() { return keto; }
    public void setKeto(Boolean keto) { this.keto = keto; }
    
    public List<String> getAllergens() { return allergens; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }
    
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    
    public Map<String, Object> getCustomizations() { return customizations; }
    public void setCustomizations(Map<String, Object> customizations) { this.customizations = customizations; }
    
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
    
    public String getPromotionalOffer() { return promotionalOffer; }
    public void setPromotionalOffer(String promotionalOffer) { this.promotionalOffer = promotionalOffer; }
}
