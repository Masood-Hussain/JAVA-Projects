package com.fooddelivery.payment.service;

import com.fooddelivery.payment.dto.*;
import com.fooddelivery.payment.entity.Payment;
import com.fooddelivery.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
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
    private FraudDetectionService fraudDetectionService;
    
    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;
    
    private final WebClient paymentGatewayClient;
    private final WebClient cryptoGatewayClient;
    private final WebClient bnplGatewayClient;
    
    // Real-time payment status updates
    private final Map<String, Sinks.Many<PaymentStatusUpdateDto>> paymentStatusSinks = new ConcurrentHashMap<>();

    public AdvancedPaymentService() {
        this.paymentGatewayClient = WebClient.builder()
            .baseUrl("https://api.stripe.com/v1") // Example gateway
            .build();
        this.cryptoGatewayClient = WebClient.builder()
            .baseUrl("https://api.coinbase.com/v2") // Example crypto gateway
            .build();
        this.bnplGatewayClient = WebClient.builder()
            .baseUrl("https://api.klarna.com/v1") // Example BNPL gateway
            .build();
    }

    /**
     * Process advanced payment with multiple gateway support
     */
    public Mono<PaymentResponseDto> processPayment(AdvancedPaymentRequestDto request) {
        return performFraudCheck(createFraudCheckRequest(request))
            .flatMap(fraudResult -> {
                if ("DECLINE".equals(fraudResult.getRecommendation())) {
                    return Mono.just(createFailedResponse("Payment declined due to fraud risk"));
                }
                
                return switch (request.getPaymentMethod()) {
                    case "CARD" -> processCardPayment(request);
                    case "CRYPTO" -> processCryptoPaymentInternal(request);
                    case "BNPL" -> processBNPLPaymentInternal(request);
                    case "DIGITAL_WALLET" -> processDigitalWalletPayment(request);
                    case "BANK_TRANSFER" -> processBankTransferPayment(request);
                    default -> Mono.just(createFailedResponse("Unsupported payment method"));
                };
            })
            .doOnNext(response -> updatePaymentStatus(response.getPaymentId(), response.getStatus()))
            .doOnNext(this::savePaymentRecord);
    }

    /**
     * Get available payment methods for user
     */
    public Flux<PaymentMethodDto> getAvailablePaymentMethods(String userId) {
        return Flux.fromIterable(getStoredPaymentMethods(userId))
            .mergeWith(getDigitalWalletMethods(userId))
            .mergeWith(getCryptoWalletMethods(userId));
    }

    /**
     * Add new payment method with tokenization
     */
    public Mono<PaymentMethodDto> addPaymentMethod(PaymentMethodRequestDto request) {
        return tokenizePaymentMethod(request)
            .flatMap(this::storePaymentMethod)
            .map(this::convertToPaymentMethodDto);
    }

    /**
     * Process cryptocurrency payment
     */
    public Mono<CryptoPaymentResponseDto> processCryptoPayment(CryptoPaymentRequestDto request) {
        return validateCryptoTransaction(request)
            .flatMap(this::initiateCryptoPayment)
            .flatMap(this::monitorCryptoConfirmations);
    }

    /**
     * Process Buy Now Pay Later payment
     */
    public Mono<BNPLPaymentResponseDto> processBNPLPayment(BNPLPaymentRequestDto request) {
        return assessBNPLEligibility(request)
            .flatMap(eligible -> {
                if (!eligible) {
                    return Mono.error(new RuntimeException("Not eligible for BNPL"));
                }
                return createBNPLInstallmentPlan(request);
            })
            .flatMap(this::processBNPLApproval);
    }

    /**
     * Process split payment among multiple parties
     */
    public Mono<SplitPaymentResponseDto> processSplitPayment(SplitPaymentRequestDto request) {
        return Flux.fromIterable(request.getSplits())
            .flatMap(this::processIndividualSplit)
            .collectList()
            .map(results -> aggregateSplitResults(request.getOrderId(), results));
    }

    /**
     * Process QR code payment
     */
    public Mono<QRPaymentResponseDto> processQRPayment(QRPaymentRequestDto request) {
        return generateQRCode(request)
            .flatMap(this::initializeQRPayment)
            .doOnNext(response -> scheduleQRExpiry(response.getPaymentId(), response.getExpirySeconds()));
    }

    /**
     * Process subscription payment
     */
    public Mono<SubscriptionPaymentResponseDto> processSubscriptionPayment(SubscriptionPaymentRequestDto request) {
        return createSubscription(request)
            .flatMap(this::scheduleRecurringPayments)
            .map(this::convertToSubscriptionResponse);
    }

    /**
     * Get payment analytics
     */
    public Mono<Map<String, Object>> getPaymentAnalytics(int days, String restaurantId) {
        return Mono.fromCallable(() -> {
            Map<String, Object> analytics = new HashMap<>();
            
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            List<Payment> payments = paymentRepository.findPaymentsByDateRange(startDate, LocalDateTime.now());
            
            if (restaurantId != null) {
                payments = payments.stream()
                    .filter(p -> restaurantId.equals(p.getRestaurantId()))
                    .toList();
            }
            
            // Calculate analytics
            BigDecimal totalAmount = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long successfulPayments = payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .count();
            
            double successRate = payments.isEmpty() ? 0.0 : 
                (double) successfulPayments / payments.size() * 100;
            
            Map<String, Long> paymentMethodStats = payments.stream()
                .collect(groupingBy(Payment::getPaymentMethod, counting()));
            
            analytics.put("totalAmount", totalAmount);
            analytics.put("totalTransactions", payments.size());
            analytics.put("successfulTransactions", successfulPayments);
            analytics.put("successRate", String.format("%.2f%%", successRate));
            analytics.put("paymentMethodBreakdown", paymentMethodStats);
            analytics.put("period", days + " days");
            analytics.put("generatedAt", LocalDateTime.now());
            
            return analytics;
        });
    }

    /**
     * Real-time payment status stream
     */
    public Flux<PaymentStatusUpdateDto> getPaymentStatusStream(String paymentId) {
        return paymentStatusSinks.computeIfAbsent(paymentId,
            k -> Sinks.many().multicast().onBackpressureBuffer())
            .asFlux();
    }

    /**
     * Process refund
     */
    public Mono<RefundResponseDto> processRefund(String paymentId, RefundRequestDto request) {
        return findPaymentById(paymentId)
            .flatMap(payment -> validateRefundEligibility(payment, request))
            .flatMap(payment -> processRefundWithGateway(payment, request))
            .doOnNext(refund -> updatePaymentStatus(paymentId, "REFUNDED"));
    }

    /**
     * Perform fraud detection check
     */
    public Mono<FraudDetectionDto> performFraudCheck(FraudCheckRequestDto request) {
        return fraudDetectionService.analyzeTransaction(request);
    }

    /**
     * Process loyalty points payment
     */
    public Mono<LoyaltyPaymentResponseDto> processLoyaltyPayment(LoyaltyPaymentRequestDto request) {
        return validateLoyaltyPoints(request)
            .flatMap(this::redeemLoyaltyPoints)
            .flatMap(result -> {
                if (result.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0) {
                    return processBackupPayment(request.getBackupPaymentMethodId(), result.getRemainingAmount())
                        .map(backup -> combinePaymentResults(result, backup));
                }
                return Mono.just(result);
            });
    }

    // Helper methods
    private Mono<PaymentResponseDto> processCardPayment(AdvancedPaymentRequestDto request) {
        return paymentGatewayClient.post()
            .uri("/charges")
            .bodyValue(buildCardPaymentRequest(request))
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> convertToPaymentResponse(response, request));
    }

    private Mono<PaymentResponseDto> processCryptoPaymentInternal(AdvancedPaymentRequestDto request) {
        return cryptoGatewayClient.post()
            .uri("/charges")
            .bodyValue(buildCryptoPaymentRequest(request))
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> convertToPaymentResponse(response, request));
    }

    private Mono<PaymentResponseDto> processBNPLPaymentInternal(AdvancedPaymentRequestDto request) {
        return bnplGatewayClient.post()
            .uri("/sessions")
            .bodyValue(buildBNPLPaymentRequest(request))
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> convertToPaymentResponse(response, request));
    }

    private Mono<PaymentResponseDto> processDigitalWalletPayment(AdvancedPaymentRequestDto request) {
        // Implementation for Apple Pay, Google Pay, etc.
        return Mono.just(createSuccessResponse(request, "Digital wallet payment processed"));
    }

    private Mono<PaymentResponseDto> processBankTransferPayment(AdvancedPaymentRequestDto request) {
        // Implementation for ACH, wire transfers, etc.
        return Mono.just(createSuccessResponse(request, "Bank transfer initiated"));
    }

    private void updatePaymentStatus(String paymentId, String status) {
        PaymentStatusUpdateDto update = new PaymentStatusUpdateDto();
        update.setPaymentId(paymentId);
        update.setStatus(status);
        update.setTimestamp(LocalDateTime.now());
        
        Sinks.Many<PaymentStatusUpdateDto> sink = paymentStatusSinks.get(paymentId);
        if (sink != null) {
            sink.tryEmitNext(update);
        }
    }

    private void savePaymentRecord(PaymentResponseDto response) {
        // Save to database
        Payment payment = new Payment();
        payment.setTransactionId(response.getTransactionId());
        payment.setAmount(response.getAmount());
        payment.setStatus(response.getStatus());
        payment.setPaymentMethod(response.getPaymentMethod());
        payment.setProcessedAt(response.getProcessedAt());
        paymentRepository.save(payment);
    }

    private PaymentResponseDto createSuccessResponse(AdvancedPaymentRequestDto request, String message) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(UUID.randomUUID().toString());
        response.setTransactionId(UUID.randomUUID().toString());
        response.setStatus("SUCCESS");
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setPaymentMethod(request.getPaymentMethod());
        response.setProcessedAt(LocalDateTime.now());
        response.setConfirmationCode(generateConfirmationCode());
        return response;
    }

    private PaymentResponseDto createFailedResponse(String reason) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(UUID.randomUUID().toString());
        response.setStatus("FAILED");
        response.setFailureReason(reason);
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }

    private String generateConfirmationCode() {
        return "FD" + System.currentTimeMillis();
    }

    // Additional helper methods would be implemented here
    private FraudCheckRequestDto createFraudCheckRequest(AdvancedPaymentRequestDto request) {
        FraudCheckRequestDto fraudCheck = new FraudCheckRequestDto();
        fraudCheck.setUserId(request.getUserId());
        fraudCheck.setAmount(request.getAmount());
        fraudCheck.setPaymentMethod(request.getPaymentMethod());
        fraudCheck.setDeviceFingerprint(request.getDeviceFingerprint());
        fraudCheck.setIpAddress(request.getIpAddress());
        fraudCheck.setUserAgent(request.getUserAgent());
        fraudCheck.setBillingAddress(request.getBillingAddress());
        return fraudCheck;
    }

    // Placeholder implementations for complex operations
    private List<PaymentMethodDto> getStoredPaymentMethods(String userId) { return new ArrayList<>(); }
    private Flux<PaymentMethodDto> getDigitalWalletMethods(String userId) { return Flux.empty(); }
    private Flux<PaymentMethodDto> getCryptoWalletMethods(String userId) { return Flux.empty(); }
    private Mono<String> tokenizePaymentMethod(PaymentMethodRequestDto request) { return Mono.just("token_123"); }
    private Mono<String> storePaymentMethod(String token) { return Mono.just(token); }
    private PaymentMethodDto convertToPaymentMethodDto(String token) { return new PaymentMethodDto(); }
    private Mono<Boolean> validateCryptoTransaction(CryptoPaymentRequestDto request) { return Mono.just(true); }
    private Mono<CryptoPaymentResponseDto> initiateCryptoPayment(Boolean valid) { return Mono.just(new CryptoPaymentResponseDto()); }
    private Mono<CryptoPaymentResponseDto> monitorCryptoConfirmations(CryptoPaymentResponseDto response) { return Mono.just(response); }
}
