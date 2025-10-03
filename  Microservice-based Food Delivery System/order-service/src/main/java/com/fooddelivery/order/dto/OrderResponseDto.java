package com.fooddelivery.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fooddelivery.common.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order responses
 */
public class OrderResponseDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("customerId")
    private Long customerId;
    
    @JsonProperty("restaurantId")
    private Long restaurantId;
    
    @JsonProperty("items")
    private List<OrderItemResponseDto> items;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("status")
    private OrderStatus status;
    
    @JsonProperty("deliveryAddress")
    private String deliveryAddress;
    
    @JsonProperty("specialInstructions")
    private String specialInstructions;
    
    @JsonProperty("customerPhone")
    private String customerPhone;
    
    @JsonProperty("customerEmail")
    private String customerEmail;
    
    @JsonProperty("deliveryFee")
    private BigDecimal deliveryFee;
    
    @JsonProperty("taxAmount")
    private BigDecimal taxAmount;
    
    @JsonProperty("discountAmount")
    private BigDecimal discountAmount;
    
    @JsonProperty("orderTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderTime;
    
    @JsonProperty("updatedTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedTime;
    
    @JsonProperty("estimatedDeliveryTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;
    
    @JsonProperty("actualDeliveryTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime actualDeliveryTime;
    
    // Default constructor
    public OrderResponseDto() {}
    
    // Constructor with essential fields
    public OrderResponseDto(Long id, Long customerId, Long restaurantId, BigDecimal totalAmount, OrderStatus status, String deliveryAddress) {
        this.id = id;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    
    public List<OrderItemResponseDto> getItems() { return items; }
    public void setItems(List<OrderItemResponseDto> items) { this.items = items; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    
    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }
    
    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }
    
    // Helper methods
    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }
    
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
    
    @Override
    public String toString() {
        return "OrderResponseDto{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", restaurantId=" + restaurantId +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", itemCount=" + getItemCount() +
                ", orderTime=" + orderTime +
                '}';
    }
}