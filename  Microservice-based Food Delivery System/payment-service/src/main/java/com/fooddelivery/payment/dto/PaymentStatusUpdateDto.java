package com.fooddelivery.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fooddelivery.common.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class PaymentStatusUpdateDto {
    
    @NotNull
    @JsonProperty("status")
    private PaymentStatus status;
    
    @JsonProperty("failureReason")
    private String failureReason;
    
    @JsonProperty("gatewayTransactionId")
    private String gatewayTransactionId;
    
    public PaymentStatusUpdateDto() {}
    
    public PaymentStatusUpdateDto(PaymentStatus status) {
        this.status = status;
    }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
}
