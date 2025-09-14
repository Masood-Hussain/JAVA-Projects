package com.fooddelivery.restaurant.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MenuSearchCriteria {
    private String searchTerm;
    private List<String> categories;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean vegetarian;
    private Boolean vegan;
    private Boolean glutenFree;
    private Boolean halal;
    private Boolean keto;
    private List<String> allergens;
    private String spiceLevel;
    private Integer preparationTimeMax;
    private String sortBy; // price, rating, popularity, preparation_time
    private String sortOrder; // asc, desc
    private Integer calories;
    private String nutritionFocus; // high_protein, low_carb, etc.
    private Boolean available;
    private String cuisine;
    private Integer page;
    private Integer size;
}
