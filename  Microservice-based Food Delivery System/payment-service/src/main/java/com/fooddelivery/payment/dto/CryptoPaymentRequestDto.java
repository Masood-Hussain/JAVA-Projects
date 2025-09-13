package com.fooddelivery.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Crypto Payment DTOs
@Data
public class CryptoPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String cryptocurrency;
    private String walletAddress;
    private String network; // ETHEREUM, BITCOIN, POLYGON, etc.
    private BigDecimal gasFee;
    private String smartContractAddress;
}

@Data
class CryptoPaymentResponseDto {
    private String paymentId;
    private String status;
    private String transactionHash;
    private String blockchainNetwork;
    private BigDecimal amountInCrypto;
    private BigDecimal exchangeRate;
    private String walletAddress;
    private Integer confirmationsRequired;
    private Integer currentConfirmations;
    private LocalDateTime expiresAt;
}

// BNPL Payment DTOs
@Data
class BNPLPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String bnplProvider; // KLARNA, AFTERPAY, SEZZLE, etc.
    private Integer installments;
    private PersonalInfoDto personalInfo;
    private CreditCheckDto creditCheck;
}

@Data
class BNPLPaymentResponseDto {
    private String paymentId;
    private String status;
    private String bnplProvider;
    private List<InstallmentDto> installmentPlan;
    private BigDecimal totalAmount;
    private BigDecimal firstPayment;
    private LocalDateTime nextPaymentDate;
    private String bnplAccountId;
}

@Data
class InstallmentDto {
    private Integer installmentNumber;
    private BigDecimal amount;
    private LocalDateTime dueDate;
    private String status;
}

// Split Payment DTOs
@Data
class SplitPaymentRequestDto {
    private String orderId;
    private BigDecimal totalAmount;
    private List<SplitPartyDto> splitParties;
    private String initiatorUserId;
}

@Data
class SplitPaymentResponseDto {
    private String paymentId;
    private String status;
    private List<SplitPaymentStatusDto> partyStatuses;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private LocalDateTime expiresAt;
}

@Data
class SplitPartyDto {
    private String userId;
    private String email;
    private BigDecimal amount;
    private String paymentMethod;
}

@Data
class SplitPaymentStatusDto {
    private String userId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paidAt;
    private String paymentMethod;
}

// QR Payment DTOs
@Data
class QRPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String qrProvider; // SQUARE, PAYPAL_QR, VENMO_QR, etc.
    private String merchantId;
}

@Data
class QRPaymentResponseDto {
    private String paymentId;
    private String qrCodeData;
    private String qrCodeImageUrl;
    private String status;
    private LocalDateTime expiresAt;
    private String deepLinkUrl;
}

// Subscription Payment DTOs
@Data
class SubscriptionPaymentRequestDto {
    private String subscriptionId;
    private String userId;
    private String planId;
    private BigDecimal amount;
    private String billingCycle; // MONTHLY, YEARLY, WEEKLY
    private String paymentMethodToken;
    private LocalDateTime startDate;
}

@Data
class SubscriptionPaymentResponseDto {
    private String subscriptionId;
    private String status;
    private String paymentId;
    private LocalDateTime nextBillingDate;
    private BigDecimal recurringAmount;
    private String billingCycle;
    private LocalDateTime createdAt;
}

// Payment Method DTOs
@Data
class PaymentMethodDto {
    private String id;
    private String type;
    private String displayName;
    private String lastFourDigits;
    private String expiryDate;
    private String brand;
    private boolean isDefault;
    private boolean isExpired;
    private LocalDateTime addedAt;
}

@Data
class PaymentMethodRequestDto {
    private String userId;
    private String type;
    private CardDetailsDto cardDetails;
    private DigitalWalletDetailsDto digitalWalletDetails;
    private BankTransferDetailsDto bankTransferDetails;
    private boolean setAsDefault;
}

// Additional DTOs
@Data
class PersonalInfoDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String ssn; // Last 4 digits
}

@Data
class CreditCheckDto {
    private boolean requireCreditCheck;
    private String creditScore;
    private String creditHistory;
}

@Data
class PaymentStatusUpdateDto {
    private String paymentId;
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> additionalData;
}

@Data
class RefundRequestDto {
    private BigDecimal amount;
    private String reason;
    private String description;
    private boolean refundShipping;
    private String refundMethod; // ORIGINAL_PAYMENT_METHOD, STORE_CREDIT, BANK_TRANSFER
}

@Data
class RefundResponseDto {
    private String refundId;
    private String status;
    private BigDecimal refundAmount;
    private String refundMethod;
    private LocalDateTime processedAt;
    private String originalPaymentId;
    private String reason;
}

@Data
class FraudCheckRequestDto {
    private String userId;
    private BigDecimal amount;
    private String ipAddress;
    private String deviceFingerprint;
    private String userAgent;
    private BillingAddressDto billingAddress;
    private String paymentMethod;
    private Map<String, Object> transactionContext;
}

@Data
class FraudDetectionDto {
    private String riskLevel;
    private Double riskScore;
    private List<String> riskFactors;
    private String recommendation;
    private String modelVersion;
    private Map<String, Object> riskDetails;
}

@Data
class LoyaltyPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal pointsToRedeem;
    private BigDecimal cashAmount;
    private String paymentMethodForCash;
}

@Data
class LoyaltyPaymentResponseDto {
    private String paymentId;
    private String status;
    private BigDecimal pointsRedeemed;
    private BigDecimal cashPaid;
    private BigDecimal pointsEarned;
    private BigDecimal remainingPoints;
    private String loyaltyTier;
}
