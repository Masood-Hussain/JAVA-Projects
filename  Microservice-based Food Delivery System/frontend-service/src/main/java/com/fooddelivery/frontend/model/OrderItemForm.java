package com.fooddelivery.frontend.model;

/**
 * Form model for order items in frontend forms
 */
public class OrderItemForm {
    
    private Long menuItemId;
    private String itemName;
    private Integer quantity;
    private Double price;
    private String specialInstructions;

    // Constructors
    public OrderItemForm() {}

    public OrderItemForm(Long menuItemId, String itemName, Integer quantity, Double price) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderItemForm(Long menuItemId, String itemName, Integer quantity, Double price, String specialInstructions) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.specialInstructions = specialInstructions;
    }

    // Getters and Setters
    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    // Helper methods
    public Double getTotalPrice() {
        if (quantity == null || price == null) {
            return 0.0;
        }
        return quantity * price;
    }

    public boolean isValid() {
        return menuItemId != null && 
               itemName != null && !itemName.trim().isEmpty() &&
               quantity != null && quantity > 0 &&
               price != null && price >= 0;
    }

    @Override
    public String toString() {
        return "OrderItemForm{" +
                "menuItemId=" + menuItemId +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", specialInstructions='" + specialInstructions + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemForm that = (OrderItemForm) o;
        return menuItemId != null ? menuItemId.equals(that.menuItemId) : that.menuItemId == null;
    }

    @Override
    public int hashCode() {
        return menuItemId != null ? menuItemId.hashCode() : 0;
    }
}