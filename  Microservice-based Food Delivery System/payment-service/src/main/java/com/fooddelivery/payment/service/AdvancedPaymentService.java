package com.fooddelivery.payment.service;

import com.fooddelivery.payment.dto.*;
import com.fooddelivery.payment.gateway.*;
import com.fooddelivery.payment.repository.PaymentRepository;
import com.fooddelivery.payment.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdvancedPaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private StripePaymentGateway stripeGateway;
    
    @Autowired
    private PayPalPaymentGateway paypalGateway;
    
    @Autowired
    private CryptoPaymentGateway cryptoGateway;
    
    @Autowired
    private FraudDetectionService fraudDetectionService;
    
    @Autowired
    private PaymentAnalyticsService analyticsService;
    
    @Autowired
    private LoyaltyService loyaltyService;

    // Real-time payment status streams
    private final Map<String, Sinks.Many<PaymentStatusUpdateDto>> paymentStatusSinks = new ConcurrentHashMap<>();

    /**
     * Process advanced payment with AI fraud detection and multi-gateway support
     */
    public Mono<PaymentResponseDto> processPayment(AdvancedPaymentRequestDto request) {
        return Mono.fromCallable(() -> {
            // Perform fraud detection first
            if (request.isEnableFraudDetection()) {
                FraudCheckRequestDto fraudCheck = createFraudCheckRequest(request);
                FraudDetectionDto fraudResult = fraudDetectionService.performFraudCheck(fraudCheck).block();
                
                if ("DECLINE".equals(fraudResult.getRecommendation())) {
                    return createFailedPaymentResponse(request, "Payment declined due to fraud risk");
                }
            }
            
            // Process loyalty points redemption if applicable
            if (request.getLoyaltyPointsToRedeem() != null) {
                loyaltyService.redeemPoints(request.getUserId(), request.getLoyaltyPointsToRedeem());
            }
            
            // Select appropriate payment gateway
            PaymentGateway gateway = selectPaymentGateway(request.getPaymentMethod());
            
            // Process payment through selected gateway
            PaymentResponseDto response = gateway.processPayment(request);
            
            // Save payment record
            savePaymentRecord(request, response);
            
            // Send real-time status update
            broadcastPaymentStatus(response);
            
            // Record analytics
            analyticsService.recordPayment(request, response);
            
            return response;
        });
    }

    /**
     * Get available payment methods for user with smart recommendations
     */
    public Flux<PaymentMethodDto> getAvailablePaymentMethods(String userId) {
        return Flux.fromIterable(Arrays.asList(
            createPaymentMethod("stripe_card", "Credit/Debit Card", "CARD", true),
            createPaymentMethod("paypal", "PayPal", "DIGITAL_WALLET", false),
            createPaymentMethod("apple_pay", "Apple Pay", "DIGITAL_WALLET", false),
            createPaymentMethod("google_pay", "Google Pay", "DIGITAL_WALLET", false),
            createPaymentMethod("crypto_btc", "Bitcoin", "CRYPTO", false),
            createPaymentMethod("crypto_eth", "Ethereum", "CRYPTO", false),
            createPaymentMethod("bnpl_klarna", "Klarna (Pay in 4)", "BNPL", false),
            createPaymentMethod("qr_upi", "UPI QR Payment", "QR", false)
        ));
    }

    /**
     * Add new payment method with tokenization
     */
    public Mono<PaymentMethodDto> addPaymentMethod(PaymentMethodRequestDto request) {
        return Mono.fromCallable(() -> {
            // Tokenize payment method for security
            String token = generateSecureToken();
            
            PaymentMethodDto method = new PaymentMethodDto();
            method.setId(token);
            method.setType(request.getType());
            method.setDisplayName(request.getDisplayName());
            method.setIsDefault(request.isSetAsDefault());
            method.setAddedAt(LocalDateTime.now());
            
            // Save encrypted payment method details
            saveEncryptedPaymentMethod(request, token);
            
            return method;
        });
    }

    /**
     * Process cryptocurrency payment with real-time blockchain tracking
     */
    public Mono<CryptoPaymentResponseDto> processCryptoPayment(CryptoPaymentRequestDto request) {
        return cryptoGateway.processCryptoPayment(request)
            .doOnNext(response -> {
                // Start blockchain confirmation monitoring
                monitorBlockchainConfirmations(response);
            });
    }

    /**
     * Process Buy Now Pay Later payment
     */
    public Mono<BNPLPaymentResponseDto> processBNPLPayment(BNPLPaymentRequestDto request) {
        return Mono.fromCallable(() -> {
            // Perform credit check
            boolean creditApproved = performCreditCheck(request);
            
            if (!creditApproved) {
                throw new RuntimeException("BNPL application declined");
            }
            
            // Create installment schedule
            List<InstallmentDto> installments = generateInstallmentSchedule(
                request.getAmount(), request.getInstallments(), request.getFrequency());
            
            BNPLPaymentResponseDto response = new BNPLPaymentResponseDto();
            response.setPaymentId(UUID.randomUUID().toString());
            response.setOrderId(request.getOrderId());
            response.setStatus("APPROVED");
            response.setBnplProvider(request.getBnplProvider());
            response.setTotalAmount(request.getAmount());
            response.setInstallments(request.getInstallments());
            response.setInstallmentAmount(request.getAmount().divide(BigDecimal.valueOf(request.getInstallments())));
            response.setInstallmentSchedule(installments);
            response.setApprovalUrl("https://bnpl-provider.com/approve/" + response.getPaymentId());
            
            return response;
        });
    }

    /**
     * Process split payment among multiple users
     */
    public Mono<SplitPaymentResponseDto> processSplitPayment(SplitPaymentRequestDto request) {
        return Mono.fromCallable(() -> {
            String splitPaymentId = UUID.randomUUID().toString();
            
            // Create split payment record
            List<SplitStatusDto> splitStatuses = new ArrayList<>();
            for (PaymentSplitDto split : request.getSplits()) {
                SplitStatusDto status = new SplitStatusDto();
                status.setUserId(split.getUserId());
                status.setEmail(split.getEmail());
                status.setAmount(split.getAmount());
                status.setStatus("PENDING");
                splitStatuses.add(status);
            }
            
            SplitPaymentResponseDto response = new SplitPaymentResponseDto();
            response.setSplitPaymentId(splitPaymentId);
            response.setOrderId(request.getOrderId());
            response.setStatus("PENDING");
            response.setTotalAmount(request.getTotalAmount());
            response.setSplitStatuses(splitStatuses);
            response.setQrCodeForSharing(generateSplitPaymentQR(splitPaymentId));
            response.setDeepLinkForSharing("fooddelivery://split-payment/" + splitPaymentId);
            
            // Send notifications to participants
            notifySplitPaymentParticipants(request.getSplits(), splitPaymentId);
            
            return response;
        });
    }

    /**
     * Process QR code payment
     */
    public Mono<QRPaymentResponseDto> processQRPayment(QRPaymentRequestDto request) {
        return Mono.fromCallable(() -> {
            String paymentId = UUID.randomUUID().toString();
            
            QRPaymentResponseDto response = new QRPaymentResponseDto();
            response.setPaymentId(paymentId);
            response.setOrderId(request.getOrderId());
            response.setStatus("PENDING");
            response.setQrCode(generatePaymentQR(request));
            response.setQrCodeUrl("https://api.fooddelivery.com/qr/" + paymentId);
            response.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            response.setPaymentProvider(request.getQrPaymentProvider());
            
            return response;
        });
    }

    /**
     * Get payment analytics with advanced insights
     */
    public Mono<Map<String, Object>> getPaymentAnalytics(int days, String restaurantId) {
        return analyticsService.getAdvancedPaymentAnalytics(days, restaurantId);
    }

    /**
     * Real-time payment status updates stream
     */
    public Flux<PaymentStatusUpdateDto> getPaymentStatusStream(String paymentId) {
        return paymentStatusSinks.computeIfAbsent(paymentId, 
            k -> Sinks.many().multicast().onBackpressureBuffer())
            .asFlux();
    }

    /**
     * Process refund with advanced options
     */
    public Mono<RefundResponseDto> processRefund(String paymentId, RefundRequestDto request) {
        return Mono.fromCallable(() -> {
            // Validate refund eligibility
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            // Process refund through original gateway
            PaymentGateway gateway = selectPaymentGateway(payment.getPaymentMethod());
            RefundResponseDto response = gateway.processRefund(paymentId, request);
            
            // Update payment status
            broadcastPaymentStatus(createStatusUpdate(paymentId, "REFUNDED", "Refund processed"));
            
            return response;
        });
    }

    /**
     * Perform AI-powered fraud detection
     */
    public Mono<FraudDetectionDto> performFraudCheck(FraudCheckRequestDto request) {
        return fraudDetectionService.performFraudCheck(request);
    }

    /**
     * Process loyalty points payment
     */
    public Mono<LoyaltyPaymentResponseDto> processLoyaltyPayment(LoyaltyPaymentRequestDto request) {
        return loyaltyService.processLoyaltyPayment(request);
    }

    // Helper methods
    private PaymentGateway selectPaymentGateway(String paymentMethod) {
        switch (paymentMethod.toUpperCase()) {
            case "CRYPTO":
                return cryptoGateway;
            case "PAYPAL":
            case "DIGITAL_WALLET":
                return paypalGateway;
            default:
                return stripeGateway;
        }
    }

    private PaymentMethodDto createPaymentMethod(String id, String displayName, String type, boolean isDefault) {
        PaymentMethodDto method = new PaymentMethodDto();
        method.setId(id);
        method.setDisplayName(displayName);
        method.setType(type);
        method.setIsDefault(isDefault);
        method.setAddedAt(LocalDateTime.now());
        return method;
    }

    private String generateSecureToken() {
        return "pm_" + UUID.randomUUID().toString().replace("-", "");
    }

    private void saveEncryptedPaymentMethod(PaymentMethodRequestDto request, String token) {
        // Implementation for saving encrypted payment method
    }

    private FraudCheckRequestDto createFraudCheckRequest(AdvancedPaymentRequestDto request) {
        FraudCheckRequestDto fraudCheck = new FraudCheckRequestDto();
        fraudCheck.setUserId(request.getUserId());
        fraudCheck.setAmount(request.getAmount());
        fraudCheck.setIpAddress(request.getIpAddress());
        fraudCheck.setDeviceFingerprint(request.getDeviceFingerprint());
        fraudCheck.setUserAgent(request.getUserAgent());
        return fraudCheck;
    }

    private PaymentResponseDto createFailedPaymentResponse(AdvancedPaymentRequestDto request, String reason) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(UUID.randomUUID().toString());
        response.setOrderId(request.getOrderId());
        response.setStatus("FAILED");
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setErrorMessage(reason);
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }

    private void savePaymentRecord(AdvancedPaymentRequestDto request, PaymentResponseDto response) {
        // Save payment to database
    }

    private void broadcastPaymentStatus(PaymentResponseDto response) {
        PaymentStatusUpdateDto update = createStatusUpdate(
            response.getPaymentId(), response.getStatus(), "Payment processed");
        
        Sinks.Many<PaymentStatusUpdateDto> sink = paymentStatusSinks.get(response.getPaymentId());
        if (sink != null) {
            sink.tryEmitNext(update);
        }
    }

    private PaymentStatusUpdateDto createStatusUpdate(String paymentId, String status, String reason) {
        PaymentStatusUpdateDto update = new PaymentStatusUpdateDto();
        update.setPaymentId(paymentId);
        update.setStatus(status);
        update.setReason(reason);
        update.setTimestamp(LocalDateTime.now());
        return update;
    }

    private void monitorBlockchainConfirmations(CryptoPaymentResponseDto response) {
        // Implementation for monitoring blockchain confirmations
    }

    private boolean performCreditCheck(BNPLPaymentRequestDto request) {
        // AI-based credit scoring
        return true; // Simplified for demo
    }

    private List<InstallmentDto> generateInstallmentSchedule(BigDecimal amount, Integer installments, String frequency) {
        List<InstallmentDto> schedule = new ArrayList<>();
        BigDecimal installmentAmount = amount.divide(BigDecimal.valueOf(installments));
        
        for (int i = 1; i <= installments; i++) {
            InstallmentDto installment = new InstallmentDto();
            installment.setInstallmentNumber(i);
            installment.setAmount(installmentAmount);
            installment.setDueDate(LocalDateTime.now().plusWeeks(i * 2)); // Biweekly
            installment.setStatus("PENDING");
            schedule.add(installment);
        }
        
        return schedule;
    }

    private String generateSplitPaymentQR(String splitPaymentId) {
        return "QR_SPLIT_" + splitPaymentId;
    }

    private String generatePaymentQR(QRPaymentRequestDto request) {
        return "QR_PAY_" + request.getOrderId();
    }

    private void notifySplitPaymentParticipants(List<PaymentSplitDto> splits, String splitPaymentId) {
        // Send notifications via email/SMS/push
    }
}
