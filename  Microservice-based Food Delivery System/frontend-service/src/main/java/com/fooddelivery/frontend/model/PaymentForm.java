package com.fooddelivery.frontend.model;

import java.time.LocalDateTime;

/**
 * Form model for payment processing from frontend
 */
public class PaymentForm {
    
    private Long orderId;
    private Double amount;
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, DIGITAL_WALLET, etc.
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String digitalWalletId;
    private String digitalWalletProvider; // PAYPAL, APPLE_PAY, GOOGLE_PAY, etc.
    private String billingAddress;
    private boolean savePaymentMethod;
    private LocalDateTime paymentTime;

    // Constructors
    public PaymentForm() {
        this.paymentTime = LocalDateTime.now();
    }

    public PaymentForm(Long orderId, Double amount, String paymentMethod) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getDigitalWalletId() {
        return digitalWalletId;
    }

    public void setDigitalWalletId(String digitalWalletId) {
        this.digitalWalletId = digitalWalletId;
    }

    public String getDigitalWalletProvider() {
        return digitalWalletProvider;
    }

    public void setDigitalWalletProvider(String digitalWalletProvider) {
        this.digitalWalletProvider = digitalWalletProvider;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public boolean isSavePaymentMethod() {
        return savePaymentMethod;
    }

    public void setSavePaymentMethod(boolean savePaymentMethod) {
        this.savePaymentMethod = savePaymentMethod;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    // Helper methods
    public boolean isCardPayment() {
        return "CREDIT_CARD".equals(paymentMethod) || "DEBIT_CARD".equals(paymentMethod);
    }

    public boolean isDigitalWallet() {
        return "DIGITAL_WALLET".equals(paymentMethod);
    }

    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public boolean isValid() {
        if (orderId == null || amount == null || amount <= 0 || paymentMethod == null) {
            return false;
        }

        if (isCardPayment()) {
            return cardNumber != null && !cardNumber.trim().isEmpty() &&
                   cardHolderName != null && !cardHolderName.trim().isEmpty() &&
                   expiryMonth != null && !expiryMonth.trim().isEmpty() &&
                   expiryYear != null && !expiryYear.trim().isEmpty() &&
                   cvv != null && !cvv.trim().isEmpty();
        }

        if (isDigitalWallet()) {
            return digitalWalletId != null && !digitalWalletId.trim().isEmpty() &&
                   digitalWalletProvider != null && !digitalWalletProvider.trim().isEmpty();
        }

        return true; // For other payment methods, basic validation is sufficient
    }

    @Override
    public String toString() {
        return "PaymentForm{" +
                "orderId=" + orderId +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", cardNumber='" + getMaskedCardNumber() + '\'' +
                ", digitalWalletProvider='" + digitalWalletProvider + '\'' +
                ", paymentTime=" + paymentTime +
                '}';
    }
}