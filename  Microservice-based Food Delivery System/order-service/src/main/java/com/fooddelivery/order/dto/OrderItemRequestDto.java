package com.fooddelivery.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for order item requests
 */
public class OrderItemRequestDto {
    
    @NotNull(message = "Menu item ID is required")
    @JsonProperty("menuItemId")
    private Long menuItemId;
    
    @NotBlank(message = "Item name is required")
    @Size(max = 200, message = "Item name cannot exceed 200 characters")
    @JsonProperty("itemName")
    private String itemName;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    @JsonProperty("quantity")
    private Integer quantity;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @JsonProperty("price")
    private BigDecimal price;
    
    @Size(max = 500, message = "Special instructions cannot exceed 500 characters")
    @JsonProperty("specialInstructions")
    private String specialInstructions;
    
    // Default constructor
    public OrderItemRequestDto() {}
    
    // Constructor with required fields
    public OrderItemRequestDto(Long menuItemId, String itemName, Integer quantity, BigDecimal price) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Constructor with all fields
    public OrderItemRequestDto(Long menuItemId, String itemName, Integer quantity, BigDecimal price, String specialInstructions) {
        this(menuItemId, itemName, quantity, price);
        this.specialInstructions = specialInstructions;
    }
    
    // Getters and Setters
    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    // Helper method to calculate total price
    public BigDecimal getTotalPrice() {
        if (price != null && quantity != null) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public String toString() {
        return "OrderItemRequestDto{" +
                "menuItemId=" + menuItemId +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}