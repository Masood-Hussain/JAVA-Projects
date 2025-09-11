package com.fooddelivery.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AdvancedPaymentRequestDto {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod; // CARD, CRYPTO, BNPL, DIGITAL_WALLET, BANK_TRANSFER
    private PaymentMethodDetailsDto paymentMethodDetails;
    private String restaurantId;
    private BillingAddressDto billingAddress;
    private Map<String, Object> metadata;
    private String returnUrl;
    private String webhookUrl;
    private boolean savePaymentMethod;
    private String loyaltyPointsToRedeem;
    private String promoCode;
    private TipDetailsDto tip;
    private boolean enableFraudDetection;
    private String deviceFingerprint;
    private String ipAddress;
    private String userAgent;
    private String merchantCategoryCode;
}

@Data
class PaymentMethodDetailsDto {
    private String type; // CREDIT_CARD, DEBIT_CARD, PAYPAL, APPLE_PAY, GOOGLE_PAY, etc.
    private String token; // Tokenized payment method
    private CardDetailsDto cardDetails;
    private DigitalWalletDetailsDto digitalWalletDetails;
    private CryptoDetailsDto cryptoDetails;
    private BankTransferDetailsDto bankTransferDetails;
}

@Data
class CardDetailsDto {
    private String encryptedCardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String encryptedCvc;
    private String holderName;
    private String cardBrand;
    private boolean is3DSRequired;
}

@Data
class DigitalWalletDetailsDto {
    private String walletType; // APPLE_PAY, GOOGLE_PAY, SAMSUNG_PAY
    private String walletToken;
    private String merchantId;
}

@Data
class CryptoDetailsDto {
    private String cryptocurrency; // BTC, ETH, USDT, etc.
    private String walletAddress;
    private String transactionHash;
    private BigDecimal exchangeRate;
}

@Data
class BankTransferDetailsDto {
    private String accountNumber;
    private String routingNumber;
    private String bankName;
    private String accountHolderName;
    private String transferType; // ACH, WIRE, INSTANT
}

@Data
class BillingAddressDto {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}

@Data
class TipDetailsDto {
    private BigDecimal tipAmount;
    private String tipType; // PERCENTAGE, FIXED_AMOUNT
    private BigDecimal tipPercentage;
}
