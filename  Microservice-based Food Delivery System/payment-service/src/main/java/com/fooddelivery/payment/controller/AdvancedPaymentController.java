package com.fooddelivery.payment.controller;

import com.fooddelivery.payment.service.AdvancedPaymentService;
import com.fooddelivery.payment.dto.*;
import com.fooddelivery.common.dto.PaymentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/payments")
@CrossOrigin(origins = "*")
public class AdvancedPaymentController {

    @Autowired
    private AdvancedPaymentService advancedPaymentService;

    /**
     * Process payment with multiple gateway support and AI fraud detection
     */
    @PostMapping("/process")
    public Mono<ResponseEntity<PaymentResponseDto>> processAdvancedPayment(
            @RequestBody AdvancedPaymentRequestDto paymentRequest) {
        return advancedPaymentService.processPayment(paymentRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get available payment methods for user
     */
    @GetMapping("/methods/{userId}")
    public Flux<PaymentMethodDto> getPaymentMethods(@PathVariable String userId) {
        return advancedPaymentService.getAvailablePaymentMethods(userId);
    }

    /**
     * Add new payment method with tokenization
     */
    @PostMapping("/methods")
    public Mono<ResponseEntity<PaymentMethodDto>> addPaymentMethod(
            @RequestBody PaymentMethodRequestDto methodRequest) {
        return advancedPaymentService.addPaymentMethod(methodRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Process cryptocurrency payment
     */
    @PostMapping("/crypto")
    public Mono<ResponseEntity<CryptoPaymentResponseDto>> processCryptoPayment(
            @RequestBody CryptoPaymentRequestDto cryptoRequest) {
        return advancedPaymentService.processCryptoPayment(cryptoRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Process Buy Now Pay Later (BNPL) payment
     */
    @PostMapping("/bnpl")
    public Mono<ResponseEntity<BNPLPaymentResponseDto>> processBNPLPayment(
            @RequestBody BNPLPaymentRequestDto bnplRequest) {
        return advancedPaymentService.processBNPLPayment(bnplRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Split payment among multiple parties
     */
    @PostMapping("/split")
    public Mono<ResponseEntity<SplitPaymentResponseDto>> processSplitPayment(
            @RequestBody SplitPaymentRequestDto splitRequest) {
        return advancedPaymentService.processSplitPayment(splitRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * QR code payment processing
     */
    @PostMapping("/qr")
    public Mono<ResponseEntity<QRPaymentResponseDto>> processQRPayment(
            @RequestBody QRPaymentRequestDto qrRequest) {
        return advancedPaymentService.processQRPayment(qrRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Process subscription payment
     */
    @PostMapping("/subscription")
    public Mono<ResponseEntity<SubscriptionPaymentResponseDto>> processSubscriptionPayment(
            @RequestBody SubscriptionPaymentRequestDto subscriptionRequest) {
        return advancedPaymentService.processSubscriptionPayment(subscriptionRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get payment analytics for admin
     */
    @GetMapping("/analytics")
    public Mono<ResponseEntity<Map<String, Object>>> getPaymentAnalytics(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String restaurantId) {
        return advancedPaymentService.getPaymentAnalytics(days, restaurantId)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Real-time payment status updates
     */
    @GetMapping("/status/{paymentId}/stream")
    public Flux<PaymentStatusUpdateDto> getPaymentStatusStream(@PathVariable String paymentId) {
        return advancedPaymentService.getPaymentStatusStream(paymentId);
    }

    /**
     * Refund payment with advanced options
     */
    @PostMapping("/{paymentId}/refund")
    public Mono<ResponseEntity<RefundResponseDto>> processRefund(
            @PathVariable String paymentId,
            @RequestBody RefundRequestDto refundRequest) {
        return advancedPaymentService.processRefund(paymentId, refundRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get fraud detection score for payment
     */
    @PostMapping("/fraud-check")
    public Mono<ResponseEntity<FraudDetectionDto>> checkFraud(
            @RequestBody FraudCheckRequestDto fraudCheckRequest) {
        return advancedPaymentService.performFraudCheck(fraudCheckRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Process loyalty points redemption
     */
    @PostMapping("/loyalty")
    public Mono<ResponseEntity<LoyaltyPaymentResponseDto>> processLoyaltyPayment(
            @RequestBody LoyaltyPaymentRequestDto loyaltyRequest) {
        return advancedPaymentService.processLoyaltyPayment(loyaltyRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
