package com.fooddelivery.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PaymentResponseDtoV2 {
    private String paymentId;
    private String orderId;
    private String status; // SUCCESS, FAILED, PENDING, PROCESSING, CANCELLED
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime processedAt;
    private String gatewayResponse;
    private FraudDetectionResultDto fraudDetection;
    private String redirectUrl; // For 3DS or other redirect flows
    private Map<String, Object> metadata;
    private String failureReason;
    private String receiptUrl;
    private BigDecimal processingFee;
    private String authorizationCode;
    private LocalDateTime expiresAt;
}

@Data
class FraudDetectionResultDto {
    private String riskScore; // LOW, MEDIUM, HIGH
    private BigDecimal score; // 0.0 to 1.0
    private String[] triggeredRules;
    private String recommendation; // APPROVE, REVIEW, DECLINE
    private Map<String, Object> details;
}

@Data
class PaymentMethodDto {
    private String id;
    private String type;
    private String lastFour;
    private String brand;
    private String expiryMonth;
    private String expiryYear;
    private boolean isDefault;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private Map<String, Object> metadata;
}

@Data
class PaymentMethodRequestDto {
    private String userId;
    private String type;
    private CardDetailsDto cardDetails;
    private DigitalWalletDetailsDto walletDetails;
    private boolean setAsDefault;
    private BillingAddressDto billingAddress;
}

@Data
class CryptoPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String cryptocurrency;
    private String walletAddress;
    private String networkFee;
    private String exchangeRateSource;
}

@Data
class CryptoPaymentResponseDto {
    private String paymentId;
    private String cryptocurrency;
    private BigDecimal cryptoAmount;
    private String walletAddress;
    private String transactionHash;
    private String blockchainNetwork;
    private BigDecimal exchangeRate;
    private String confirmationStatus;
    private int confirmationsRequired;
    private int currentConfirmations;
}

@Data
class BNPLPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String bnplProvider; // KLARNA, AFTERPAY, AFFIRM, SEZZLE
    private int installments;
    private String frequency; // WEEKLY, BI_WEEKLY, MONTHLY
}

@Data
class BNPLPaymentResponseDto {
    private String paymentId;
    private String bnplProvider;
    private String bnplTransactionId;
    private int installments;
    private BigDecimal installmentAmount;
    private LocalDateTime firstPaymentDate;
    private LocalDateTime lastPaymentDate;
    private String approvalStatus;
    private String redirectUrl;
}

@Data
class SplitPaymentRequestDto {
    private String orderId;
    private BigDecimal totalAmount;
    private SplitParticipantDto[] participants;
    private String splitType; // EQUAL, CUSTOM, BY_ITEM
    private Map<String, Object> splitRules;
}

@Data
class SplitParticipantDto {
    private String userId;
    private String email;
    private BigDecimal amount;
    private String paymentMethodId;
    private String status; // PENDING, PAID, FAILED
}

@Data
class SplitPaymentResponseDto {
    private String splitPaymentId;
    private String orderId;
    private BigDecimal totalAmount;
    private int totalParticipants;
    private int paidParticipants;
    private String overallStatus;
    private SplitParticipantDto[] participants;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

@Data
class QRPaymentRequestDto {
    private String orderId;
    private BigDecimal amount;
    private String qrType; // DYNAMIC, STATIC
    private String paymentMethod; // UPI, ALIPAY, WECHAT_PAY
    private int expiryMinutes;
}

@Data
class QRPaymentResponseDto {
    private String paymentId;
    private String qrCode;
    private String qrCodeUrl;
    private String deepLinkUrl;
    private LocalDateTime expiresAt;
    private String status;
}

@Data
class SubscriptionPaymentRequestDto {
    private String userId;
    private String planId;
    private BigDecimal amount;
    private String frequency; // DAILY, WEEKLY, MONTHLY, YEARLY
    private String paymentMethodId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int trialDays;
}

@Data
class SubscriptionPaymentResponseDto {
    private String subscriptionId;
    private String paymentId;
    private String planId;
    private String status;
    private LocalDateTime nextBillingDate;
    private BigDecimal recurringAmount;
    private int remainingPayments;
}

@Data
class PaymentStatusUpdateDto {
    private String paymentId;
    private String status;
    private String previousStatus;
    private LocalDateTime updatedAt;
    private String reason;
    private Map<String, Object> additionalData;
}

@Data
class RefundRequestDto {
    private BigDecimal amount; // Partial refund if less than original
    private String reason;
    private String refundMethod; // ORIGINAL_PAYMENT_METHOD, BANK_ACCOUNT, STORE_CREDIT
    private boolean notifyCustomer;
    private Map<String, Object> metadata;
}

@Data
class RefundResponseDto {
    private String refundId;
    private String paymentId;
    private BigDecimal refundAmount;
    private String status;
    private String refundMethod;
    private LocalDateTime processedAt;
    private String transactionId;
    private String estimatedArrival;
}

@Data
class FraudCheckRequestDto {
    private String userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String ipAddress;
    private String deviceFingerprint;
    private String userAgent;
    private BillingAddressDto billingAddress;
    private Map<String, Object> transactionContext;
}

@Data
class FraudDetectionDto {
    private String riskLevel;
    private BigDecimal riskScore;
    private String recommendation;
    private String[] triggeredRules;
    private Map<String, Object> riskFactors;
    private String analysisId;
}

@Data
class LoyaltyPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal orderAmount;
    private int loyaltyPointsToRedeem;
    private BigDecimal pointsValue;
    private BigDecimal remainingAmount;
    private String fallbackPaymentMethodId;
}

@Data
class LoyaltyPaymentResponseDto {
    private String paymentId;
    private int pointsRedeemed;
    private BigDecimal pointsValue;
    private int remainingPoints;
    private BigDecimal remainingAmount;
    private String fallbackPaymentId;
    private String status;
}
