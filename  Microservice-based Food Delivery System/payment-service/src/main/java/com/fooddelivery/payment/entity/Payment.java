package com.fooddelivery.payment.entity;

import com.fooddelivery.common.enums.PaymentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_order_id", columnList = "orderId"),
    @Index(name = "idx_transaction_id", columnList = "transactionId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_payment_time", columnList = "paymentTime")
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency = "USD";

    @Column(nullable = false, length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(unique = true, length = 100)
    private String transactionId;
    
    @Column
    private LocalDateTime paymentTime;
    
    @Column(length = 500)
    private String failureReason;
    
    @Column(length = 1000)
    private String description;
    
    // Customer information
    @Column(length = 255)
    private String customerEmail;
    
    @Column(length = 255)
    private String customerName;
    
    @Column(length = 20)
    private String customerPhone;
    
    // Billing address
    @Column(length = 500)
    private String billingAddress;
    
    @Column(length = 100)
    private String billingCity;
    
    @Column(length = 100)
    private String billingState;
    
    @Column(length = 20)
    private String billingZip;
    
    @Column(length = 100)
    private String billingCountry;
    
    // Card information (tokenized/masked)
    @Column(length = 100)
    private String cardToken;
    
    @Column(length = 4)
    private String cardLast4;
    
    @Column(length = 20)
    private String cardBrand;
    
    // Gateway specific information
    @Column(length = 100)
    private String gatewayTransactionId;
    
    @Column(length = 1000)
    private String gatewayResponse;
    
    @Column(length = 500)
    private String receiptUrl;
    
    // Processing information
    @Column(precision = 10, scale = 2)
    private BigDecimal processingFee;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal netAmount;
    
    @Column
    private LocalDateTime processedAt;
    
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // JSON field for metadata (using String to store JSON)
    @Column(columnDefinition = "TEXT")
    private String metadata;

    // Constructors
    public Payment() {}

    public Payment(Long orderId, BigDecimal amount, String paymentMethod) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
        this.netAmount = amount; // Initially, net amount equals total amount
    }
    
    public Payment(Long orderId, BigDecimal amount, String currency, String paymentMethod, 
                   String customerEmail, String customerName) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.paymentTime = LocalDateTime.now();
        this.netAmount = amount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    
    public String getBillingCity() { return billingCity; }
    public void setBillingCity(String billingCity) { this.billingCity = billingCity; }
    
    public String getBillingState() { return billingState; }
    public void setBillingState(String billingState) { this.billingState = billingState; }
    
    public String getBillingZip() { return billingZip; }
    public void setBillingZip(String billingZip) { this.billingZip = billingZip; }
    
    public String getBillingCountry() { return billingCountry; }
    public void setBillingCountry(String billingCountry) { this.billingCountry = billingCountry; }
    
    public String getCardToken() { return cardToken; }
    public void setCardToken(String cardToken) { this.cardToken = cardToken; }
    
    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }
    
    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
    
    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
    
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    
    public BigDecimal getProcessingFee() { return processingFee; }
    public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }
    
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    // Utility methods
    public boolean isPending() { return status == PaymentStatus.PENDING; }
    public boolean isProcessing() { return status == PaymentStatus.PROCESSING; }
    public boolean isCompleted() { return status == PaymentStatus.COMPLETED; }
    public boolean isFailed() { return status == PaymentStatus.FAILED; }
    public boolean isRefunded() { return status == PaymentStatus.REFUNDED; }
    
    public void markAsProcessing() {
        this.status = PaymentStatus.PROCESSING;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsCompleted(String transactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsRefunded() {
        this.status = PaymentStatus.REFUNDED;
        this.processedAt = LocalDateTime.now();
    }
}
