package com.fooddelivery.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fooddelivery.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for order status updates
 */
public class OrderStatusUpdateDto {
    
    @NotNull(message = "Status is required")
    @JsonProperty("status")
    private OrderStatus status;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("estimatedDeliveryTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;
    
    @JsonProperty("actualDeliveryTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime actualDeliveryTime;
    
    // Default constructor
    public OrderStatusUpdateDto() {}
    
    // Constructor with status
    public OrderStatusUpdateDto(OrderStatus status) {
        this.status = status;
    }
    
    // Constructor with status and reason
    public OrderStatusUpdateDto(OrderStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }
    
    // Getters and Setters
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }
    
    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }
    
    @Override
    public String toString() {
        return "OrderStatusUpdateDto{" +
                "status=" + status +
                ", reason='" + reason + '\'' +
                ", estimatedDeliveryTime=" + estimatedDeliveryTime +
                ", actualDeliveryTime=" + actualDeliveryTime +
                '}';
    }
}