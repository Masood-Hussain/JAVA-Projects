package com.fooddelivery.frontend.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Form model for order submission from frontend
 */
public class OrderForm {
    
    private Long customerId;
    private Long restaurantId;
    private String deliveryAddress;
    private String specialInstructions;
    private String customerPhone;
    private String customerEmail;
    private Double deliveryFee;
    private Double taxAmount;
    private Double discountAmount;
    private LocalDateTime estimatedDeliveryTime;
    private List<OrderItemForm> items;

    // Constructors
    public OrderForm() {}

    public OrderForm(Long customerId, Long restaurantId, String deliveryAddress) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress;
    }

    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(Double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public List<OrderItemForm> getItems() {
        return items;
    }

    public void setItems(List<OrderItemForm> items) {
        this.items = items;
    }

    // Helper methods
    public Double calculateSubtotal() {
        if (items == null) return 0.0;
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public Double calculateTotal() {
        Double subtotal = calculateSubtotal();
        Double delivery = deliveryFee != null ? deliveryFee : 0.0;
        Double tax = taxAmount != null ? taxAmount : 0.0;
        Double discount = discountAmount != null ? discountAmount : 0.0;
        return subtotal + delivery + tax - discount;
    }

    @Override
    public String toString() {
        return "OrderForm{" +
                "customerId=" + customerId +
                ", restaurantId=" + restaurantId +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", items=" + (items != null ? items.size() : 0) +
                '}';
    }
}