package com.fooddelivery.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating order requests
 */
public class OrderRequestDto {
    
    @NotNull(message = "Customer ID is required")
    @JsonProperty("customerId")
    private Long customerId;
    
    @NotNull(message = "Restaurant ID is required")
    @JsonProperty("restaurantId")
    private Long restaurantId;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @JsonProperty("items")
    private List<OrderItemRequestDto> items;
    
    @NotBlank(message = "Delivery address is required")
    @Size(max = 500, message = "Delivery address cannot exceed 500 characters")
    @JsonProperty("deliveryAddress")
    private String deliveryAddress;
    
    @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
    @JsonProperty("specialInstructions")
    private String specialInstructions;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @JsonProperty("customerPhone")
    private String customerPhone;
    
    @Email(message = "Invalid email format")
    @JsonProperty("customerEmail")
    private String customerEmail;
    
    @DecimalMin(value = "0.00", message = "Delivery fee cannot be negative")
    @JsonProperty("deliveryFee")
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    @JsonProperty("taxAmount")
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
    @JsonProperty("discountAmount")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @JsonProperty("estimatedDeliveryTime")
    private LocalDateTime estimatedDeliveryTime;
    
    // Default constructor
    public OrderRequestDto() {}
    
    // Constructor with required fields
    public OrderRequestDto(Long customerId, Long restaurantId, List<OrderItemRequestDto> items, String deliveryAddress) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
    }
    
    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    
    public List<OrderItemRequestDto> getItems() { return items; }
    public void setItems(List<OrderItemRequestDto> items) { this.items = items; }
    
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
    
    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }
    
    @Override
    public String toString() {
        return "OrderRequestDto{" +
                "customerId=" + customerId +
                ", restaurantId=" + restaurantId +
                ", itemCount=" + (items != null ? items.size() : 0) +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", deliveryFee=" + deliveryFee +
                '}';
    }
}