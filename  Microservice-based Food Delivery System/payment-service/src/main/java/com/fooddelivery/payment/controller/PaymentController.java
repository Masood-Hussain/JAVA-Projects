package com.fooddelivery.payment.controller;

import com.fooddelivery.payment.service.PaymentService;
import com.fooddelivery.payment.dto.*;
import com.fooddelivery.common.dto.PaymentDto;
import com.fooddelivery.common.enums.PaymentStatus;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    // Enhanced endpoints using new DTOs
    
    @PostMapping("/process")
    public ResponseEntity<?> processPaymentEnhanced(@Valid @RequestBody PaymentRequestDto request) {
        try {
            logger.info("Processing payment request for order: {}", request.getOrderId());
            PaymentResponseDto response = paymentService.processPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Payment processing failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment processing failed"));
        }
    }
    
    @GetMapping("/enhanced")
    public ResponseEntity<List<PaymentResponseDto>> getAllPaymentsEnhanced() {
        List<PaymentResponseDto> payments = paymentService.getAllPaymentsEnhanced();
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/{paymentId}/enhanced")
    public ResponseEntity<PaymentResponseDto> getPaymentByIdEnhanced(@PathVariable Long paymentId) {
        Optional<PaymentResponseDto> payment = paymentService.getPaymentByIdEnhanced(paymentId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/order/{orderId}/enhanced")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderIdEnhanced(@PathVariable Long orderId) {
        Optional<PaymentResponseDto> payment = paymentService.getPaymentByOrderIdEnhanced(orderId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    @PutMapping("/{paymentId}/status/enhanced")
    public ResponseEntity<?> updatePaymentStatusEnhanced(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentStatusUpdateDto updateDto) {
        try {
            PaymentResponseDto updated = paymentService.updatePaymentStatusEnhanced(paymentId, updateDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to update payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Status update failed"));
        }
    }
    
    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<?> verifyPayment(@PathVariable Long paymentId) {
        try {
            PaymentResponseDto verified = paymentService.verifyPayment(paymentId);
            return ResponseEntity.ok(verified);
        } catch (Exception e) {
            logger.error("Payment verification failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment verification failed"));
        }
    }
    
    // Legacy endpoints for backward compatibility
    
    @PostMapping
    public ResponseEntity<PaymentDto> processPayment(@RequestBody PaymentDto paymentDto) {
        PaymentDto processed = paymentService.processPayment(paymentDto);
        return ResponseEntity.ok(processed);
    }

    @GetMapping
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        List<PaymentDto> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto> getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentDto> updatePaymentStatus(
            @PathVariable Long paymentId, 
            @RequestParam PaymentStatus status) {
        PaymentDto updated = paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(updated);
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "payment-service",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
