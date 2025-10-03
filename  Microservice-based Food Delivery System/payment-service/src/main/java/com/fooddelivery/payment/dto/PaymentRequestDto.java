package com.fooddelivery.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PaymentRequestDto {
    
    @NotNull
    @JsonProperty("orderId")
    private Long orderId;
    
    @NotNull
    @JsonProperty("customerId")
    private Long customerId;
    
    @NotNull
    @DecimalMin("0.01")
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @NotBlank
    @JsonProperty("currency")
    private String currency;
    
    @NotBlank
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    
    public PaymentRequestDto() {}
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
