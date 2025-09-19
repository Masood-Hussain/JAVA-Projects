package com.fooddelivery.restaurant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
    
    private String category;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    // Advanced 2025 features
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "ingredients", length = 1000)
    private String ingredients; // Comma-separated list
    
    @Column(name = "nutritional_info", length = 1000)
    private String nutritionalInfo; // JSON format
    
    @Column(name = "preparation_time")
    private Integer preparationTime; // in minutes
    
    @Column(name = "spice_level")
    private String spiceLevel;
    
    private Integer calories;
    
    @Column(name = "is_vegetarian")
    private Boolean vegetarian = false;
    
    @Column(name = "is_vegan")
    private Boolean vegan = false;
    
    @Column(name = "is_gluten_free")
    private Boolean glutenFree = false;
    
    @Column(name = "is_halal")
    private Boolean halal = false;
    
    @Column(name = "is_keto")
    private Boolean keto = false;
    
    @Column(name = "allergens", length = 500)
    private String allergens; // Comma-separated list
    
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;
    
    @Column(name = "review_count")
    private Integer reviewCount = 0;
    
    private String cuisine;
    
    @Column(name = "is_featured")
    private Boolean featured = false;
    
    @Column(name = "promotional_offer")
    private String promotionalOffer;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Constructors
    public MenuItem() {}

    public MenuItem(String name, String description, BigDecimal price, 
                   Restaurant restaurant, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.restaurant = restaurant;
        this.category = category;
        this.isAvailable = true;
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

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    
    // Convenience methods for restaurant ID
    public Long getRestaurantId() { 
        return restaurant != null ? restaurant.getId() : null; 
    }
    
    public void setRestaurantId(Long restaurantId) {
        if (restaurantId != null) {
            Restaurant r = new Restaurant();
            r.setId(restaurantId);
            this.restaurant = r;
        } else {
            this.restaurant = null;
        }
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    
    public Boolean getAvailable() { return isAvailable; }
    public void setAvailable(Boolean available) { this.isAvailable = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Advanced feature getters and setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    
    public String getNutritionalInfo() { return nutritionalInfo; }
    public void setNutritionalInfo(String nutritionalInfo) { this.nutritionalInfo = nutritionalInfo; }
    
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
    
    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }
    
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
    
    public String getPromotionalOffer() { return promotionalOffer; }
    public void setPromotionalOffer(String promotionalOffer) { this.promotionalOffer = promotionalOffer; }
}
