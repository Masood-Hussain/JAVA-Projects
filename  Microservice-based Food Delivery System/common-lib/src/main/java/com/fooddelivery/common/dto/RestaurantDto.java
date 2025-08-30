package com.fooddelivery.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class RestaurantDto {
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String address;
    
    @NotBlank
    private String phone;
    
    @NotBlank
    private String cuisine;
    
    private String description;
    
    private Double rating;
    
    private Boolean isActive;

    // Constructors
    public RestaurantDto() {}

    public RestaurantDto(Long id, String name, String address, String phone, 
                        String cuisine, String description, Double rating, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.cuisine = cuisine;
        this.description = description;
        this.rating = rating;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
