package com.fooddelivery.common.dto;

import java.math.BigDecimal;

public class OrderItemDto {
    private Long id;
    private Long menuItemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;

    // Constructors
    public OrderItemDto() {}

    public OrderItemDto(Long menuItemId, String itemName, Integer quantity, BigDecimal price) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
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
}
