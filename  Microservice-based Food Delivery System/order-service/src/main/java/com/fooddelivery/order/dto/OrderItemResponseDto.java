package com.fooddelivery.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * DTO for order item responses
 */
public class OrderItemResponseDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("menuItemId")
    private Long menuItemId;
    
    @JsonProperty("itemName")
    private String itemName;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("totalPrice")
    private BigDecimal totalPrice;
    
    @JsonProperty("specialInstructions")
    private String specialInstructions;
    
    // Default constructor
    public OrderItemResponseDto() {}
    
    // Constructor with essential fields
    public OrderItemResponseDto(Long id, Long menuItemId, String itemName, Integer quantity, BigDecimal price, BigDecimal totalPrice) {
        this.id = id;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
    }
    
    // Constructor with all fields
    public OrderItemResponseDto(Long id, Long menuItemId, String itemName, Integer quantity, BigDecimal price, BigDecimal totalPrice, String specialInstructions) {
        this(id, menuItemId, itemName, quantity, price, totalPrice);
        this.specialInstructions = specialInstructions;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    @Override
    public String toString() {
        return "OrderItemResponseDto{" +
                "id=" + id +
                ", menuItemId=" + menuItemId +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", totalPrice=" + totalPrice +
                '}';
    }
}