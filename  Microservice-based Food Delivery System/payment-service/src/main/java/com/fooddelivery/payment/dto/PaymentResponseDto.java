package com.fooddelivery.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PaymentResponseDto {
    private String paymentId;
    private String orderId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String transactionId;
    private String gatewayResponse;
    private LocalDateTime processedAt;
    private String receiptUrl;
    private FraudDetectionResultDto fraudDetection;
    private Map<String, Object> metadata;
    private String nextAction; // For 3DS or additional verification
    private String errorCode;
    private String errorMessage;
}

@Data
public class PaymentMethodDto {
    private String id;
    private String type;
    private String displayName;
    private boolean isDefault;
    private String lastFourDigits;
    private String brand;
    private String expiryMonth;
    private String expiryYear;
    private boolean isExpired;
    private LocalDateTime addedAt;
    private Map<String, Object> metadata;
}

@Data
public class CryptoPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String cryptocurrency;
    private String walletAddress;
    private String networkType; // MAINNET, TESTNET
    private BigDecimal gasFee;
    private Integer confirmationBlocks;
}

@Data
public class CryptoPaymentResponseDto {
    private String paymentId;
    private String orderId;
    private String status;
    private String cryptocurrency;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private String walletAddress;
    private String transactionHash;
    private String blockchainNetwork;
    private Integer confirmationsRequired;
    private Integer currentConfirmations;
    private LocalDateTime estimatedConfirmationTime;
    private String qrCode;
}

@Data
public class BNPLPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String bnplProvider; // KLARNA, AFTERPAY, AFFIRM, SEZZLE
    private Integer installments;
    private String frequency; // WEEKLY, BIWEEKLY, MONTHLY
    private BillingAddressDto billingAddress;
}

@Data
public class BNPLPaymentResponseDto {
    private String paymentId;
    private String orderId;
    private String status;
    private String bnplProvider;
    private BigDecimal totalAmount;
    private Integer installments;
    private BigDecimal installmentAmount;
    private List<InstallmentDto> installmentSchedule;
    private String approvalUrl;
    private String termsUrl;
}

@Data
public class SplitPaymentRequestDto {
    private String orderId;
    private BigDecimal totalAmount;
    private List<PaymentSplitDto> splits;
    private String initiatorUserId;
    private String splitType; // EQUAL, CUSTOM, BY_ITEM
}

@Data
public class SplitPaymentResponseDto {
    private String splitPaymentId;
    private String orderId;
    private String status;
    private BigDecimal totalAmount;
    private List<SplitStatusDto> splitStatuses;
    private String qrCodeForSharing;
    private String deepLinkForSharing;
}

@Data
public class QRPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String qrCodeData;
    private String qrPaymentProvider; // UPI, ALIPAY, WECHAT_PAY
}

@Data
public class QRPaymentResponseDto {
    private String paymentId;
    private String orderId;
    private String status;
    private String qrCode;
    private String qrCodeUrl;
    private LocalDateTime expiresAt;
    private String paymentProvider;
}

@Data
public class PaymentStatusUpdateDto {
    private String paymentId;
    private String orderId;
    private String status;
    private String previousStatus;
    private LocalDateTime timestamp;
    private String reason;
    private Map<String, Object> additionalInfo;
}

@Data
public class FraudDetectionResultDto {
    private String riskScore; // LOW, MEDIUM, HIGH
    private BigDecimal score; // 0.0 to 1.0
    private List<String> riskFactors;
    private String recommendation; // APPROVE, REVIEW, DECLINE
    private String aiModel;
    private String deviceRiskScore;
    private String behaviorAnalysis;
}

@Data
class InstallmentDto {
    private Integer installmentNumber;
    private BigDecimal amount;
    private LocalDateTime dueDate;
    private String status; // PENDING, PAID, OVERDUE
}

@Data
class PaymentSplitDto {
    private String userId;
    private String email;
    private BigDecimal amount;
    private String paymentMethodId;
    private String status; // PENDING, COMPLETED, FAILED
}

@Data
class SplitStatusDto {
    private String userId;
    private String email;
    private BigDecimal amount;
    private String status;
    private String paymentId;
    private LocalDateTime paidAt;
}
