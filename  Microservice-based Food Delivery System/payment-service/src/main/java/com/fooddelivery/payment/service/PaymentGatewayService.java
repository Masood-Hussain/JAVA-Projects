package com.fooddelivery.payment.service;

import com.fooddelivery.payment.dto.PaymentRequestDto;
import com.fooddelivery.common.enums.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Mock Payment Gateway Service
 * In a real application, this would integrate with actual payment gateways like Stripe, PayPal, etc.
 */
@Service
public class PaymentGatewayService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayService.class);
    private final Random random = new Random();
    
    /**
     * Process payment through mock gateway
     */
    public PaymentGatewayResult processPayment(PaymentRequestDto request) {
        logger.info("Processing payment for order {} with amount {} via {}", 
                   request.getOrderId(), request.getAmount(), request.getPaymentMethod());
        
        // Simulate processing delay
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        PaymentGatewayResult result = new PaymentGatewayResult();
        result.setTransactionId(UUID.randomUUID().toString());
        result.setGatewayTransactionId("gw_" + UUID.randomUUID().toString().substring(0, 8));
        
        // Simulate different payment method behaviors
        switch (request.getPaymentMethod().toLowerCase()) {
            case "credit_card":
            case "debit_card":
                result = processCreditCardPayment(request, result);
                break;
            case "paypal":
                result = processPayPalPayment(request, result);
                break;
            case "apple_pay":
            case "google_pay":
                result = processDigitalWalletPayment(request, result);
                break;
            case "bank_transfer":
                result = processBankTransferPayment(request, result);
                break;
            default:
                result.setStatus(PaymentStatus.FAILED);
                result.setFailureReason("Unsupported payment method: " + request.getPaymentMethod());
        }
        
        logger.info("Payment processing completed for order {} with status: {}", 
                   request.getOrderId(), result.getStatus());
        
        return result;
    }
    
    private PaymentGatewayResult processCreditCardPayment(PaymentRequestDto request, PaymentGatewayResult result) {
        // Simulate 92% success rate for credit cards
        if (random.nextDouble() < 0.92) {
            result.setStatus(PaymentStatus.COMPLETED);
            result.setGatewayResponse("Payment successful");
            result.setReceiptUrl("https://receipts.example.com/" + result.getTransactionId());
        } else {
            result.setStatus(PaymentStatus.FAILED);
            String[] failureReasons = {
                "Insufficient funds",
                "Card expired",
                "Invalid card number",
                "Card declined by issuer",
                "Security check failed"
            };
            result.setFailureReason(failureReasons[random.nextInt(failureReasons.length)]);
            result.setGatewayResponse("Payment declined");
        }
        return result;
    }
    
    private PaymentGatewayResult processPayPalPayment(PaymentRequestDto request, PaymentGatewayResult result) {
        // Simulate 95% success rate for PayPal
        if (random.nextDouble() < 0.95) {
            result.setStatus(PaymentStatus.COMPLETED);
            result.setGatewayResponse("PayPal payment successful");
            result.setReceiptUrl("https://paypal.com/receipt/" + result.getTransactionId());
        } else {
            result.setStatus(PaymentStatus.FAILED);
            result.setFailureReason("PayPal account has insufficient funds");
            result.setGatewayResponse("PayPal payment failed");
        }
        return result;
    }
    
    private PaymentGatewayResult processDigitalWalletPayment(PaymentRequestDto request, PaymentGatewayResult result) {
        // Simulate 97% success rate for digital wallets (Apple Pay, Google Pay)
        if (random.nextDouble() < 0.97) {
            result.setStatus(PaymentStatus.COMPLETED);
            result.setGatewayResponse("Digital wallet payment successful");
            result.setReceiptUrl("https://wallet.example.com/receipt/" + result.getTransactionId());
        } else {
            result.setStatus(PaymentStatus.FAILED);
            result.setFailureReason("Digital wallet authentication failed");
            result.setGatewayResponse("Wallet payment failed");
        }
        return result;
    }
    
    private PaymentGatewayResult processBankTransferPayment(PaymentRequestDto request, PaymentGatewayResult result) {
        // Bank transfers are usually pending initially
        result.setStatus(PaymentStatus.PROCESSING);
        result.setGatewayResponse("Bank transfer initiated");
        result.setFailureReason("Processing - will complete in 1-3 business days");
        return result;
    }
    
    /**
     * Process refund through mock gateway
     */
    public RefundResult processRefund(String originalTransactionId, BigDecimal amount, String reason) {
        logger.info("Processing refund for transaction {} with amount {}", originalTransactionId, amount);
        
        // Simulate processing delay
        try {
            Thread.sleep(500 + random.nextInt(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        RefundResult result = new RefundResult();
        result.setRefundTransactionId("ref_" + UUID.randomUUID().toString().substring(0, 8));
        
        // Simulate 98% success rate for refunds
        if (random.nextDouble() < 0.98) {
            result.setStatus("COMPLETED");
            result.setGatewayResponse("Refund processed successfully");
        } else {
            result.setStatus("FAILED");
            result.setFailureReason("Original transaction not found or not refundable");
            result.setGatewayResponse("Refund failed");
        }
        
        logger.info("Refund processing completed for transaction {} with status: {}", 
                   originalTransactionId, result.getStatus());
        
        return result;
    }
    
    /**
     * Verify payment status with gateway
     */
    public PaymentVerificationResult verifyPayment(String transactionId) {
        logger.info("Verifying payment status for transaction: {}", transactionId);
        
        PaymentVerificationResult result = new PaymentVerificationResult();
        result.setTransactionId(transactionId);
        
        // Simulate verification (in real implementation, this would call the actual gateway)
        if (random.nextDouble() < 0.95) {
            result.setStatus(PaymentStatus.COMPLETED);
            result.setVerified(true);
            result.setGatewayResponse("Payment verified successfully");
        } else {
            result.setStatus(PaymentStatus.FAILED);
            result.setVerified(false);
            result.setGatewayResponse("Payment verification failed");
        }
        
        return result;
    }
    
    // Inner classes for gateway responses
    public static class PaymentGatewayResult {
        private String transactionId;
        private String gatewayTransactionId;
        private PaymentStatus status;
        private String failureReason;
        private String gatewayResponse;
        private String receiptUrl;
        private Map<String, String> metadata = new HashMap<>();
        
        // Getters and Setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getGatewayTransactionId() { return gatewayTransactionId; }
        public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
        
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }
        
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        
        public String getGatewayResponse() { return gatewayResponse; }
        public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
        
        public String getReceiptUrl() { return receiptUrl; }
        public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }
    
    public static class RefundResult {
        private String refundTransactionId;
        private String status;
        private String failureReason;
        private String gatewayResponse;
        
        // Getters and Setters
        public String getRefundTransactionId() { return refundTransactionId; }
        public void setRefundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        
        public String getGatewayResponse() { return gatewayResponse; }
        public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
    }
    
    public static class PaymentVerificationResult {
        private String transactionId;
        private PaymentStatus status;
        private boolean verified;
        private String gatewayResponse;
        
        // Getters and Setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }
        
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
        
        public String getGatewayResponse() { return gatewayResponse; }
        public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
    }
}