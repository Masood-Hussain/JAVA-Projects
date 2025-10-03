package com.fooddelivery.payment.service;

import com.fooddelivery.payment.entity.Payment;
import com.fooddelivery.payment.repository.PaymentRepository;
import com.fooddelivery.payment.dto.*;
import com.fooddelivery.common.dto.PaymentDto;
import com.fooddelivery.common.enums.PaymentStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    // Processing fee rate (2.9% + $0.30 for card payments)
    private static final BigDecimal CARD_PROCESSING_RATE = new BigDecimal("0.029");
    private static final BigDecimal CARD_PROCESSING_FEE = new BigDecimal("0.30");
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    


    /**
     * Process payment using enhanced PaymentRequestDto
     */
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        logger.info("Processing payment for order {} with amount {} {}", 
                   request.getOrderId(), request.getAmount(), request.getCurrency());
        
        try {
            // Validate request
            validatePaymentRequest(request);
            
            // Create payment entity
            Payment payment = createPaymentFromRequest(request);
            
            // Calculate processing fees
            calculateProcessingFees(payment);
            
            // Save initial payment record
            payment.markAsProcessing();
            payment = paymentRepository.save(payment);
            
            // Process through payment gateway
            PaymentGatewayService.PaymentGatewayResult gatewayResult = 
                paymentGatewayService.processPayment(request);
            
            // Update payment with gateway response
            updatePaymentWithGatewayResult(payment, gatewayResult);
            
            // Save final payment state
            payment = paymentRepository.save(payment);
            
            logger.info("Payment processing completed for order {} with status: {}", 
                       request.getOrderId(), payment.getStatus());
            
            return convertToResponseDto(payment);
            
        } catch (Exception e) {
            logger.error("Payment processing failed for order {}: {}", request.getOrderId(), e.getMessage(), e);
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Legacy method for backward compatibility
     */
    public PaymentDto processPayment(PaymentDto paymentDto) {
        // Convert to new request format
        PaymentRequestDto request = new PaymentRequestDto();
        request.setOrderId(paymentDto.getOrderId());
        request.setAmount(paymentDto.getAmount());
        request.setPaymentMethod(paymentDto.getPaymentMethod());
        
        // Process using new method
        PaymentResponseDto response = processPayment(request);
        
        // Convert back to legacy format
        return convertResponseToLegacyDto(response);
    }

    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
            .map(this::convertToDto)
            .toList();
    }

    public Optional<PaymentDto> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .map(this::convertToDto);
    }

    public PaymentDto updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(status);
        payment = paymentRepository.save(payment);
        return convertToDto(payment);
    }

    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrderId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentTime(payment.getPaymentTime());
        dto.setFailureReason(payment.getFailureReason());
        return dto;
    }
    
    // New enhanced methods
    
    private void validatePaymentRequest(PaymentRequestDto request) {
        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new IllegalArgumentException("Valid order ID is required");
        }
        
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        
        if (request.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds maximum limit");
        }
        
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        
        // Validate currency
        if (request.getCurrency() != null && !isValidCurrency(request.getCurrency())) {
            throw new IllegalArgumentException("Unsupported currency: " + request.getCurrency());
        }
    }
    
    private boolean isValidCurrency(String currency) {
        return currency.matches("USD|EUR|GBP|CAD|AUD|INR|JPY");
    }
    
    private Payment createPaymentFromRequest(PaymentRequestDto request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        payment.setPaymentMethod(request.getPaymentMethod());
        
        return payment;
    }
    
    private void calculateProcessingFees(Payment payment) {
        if ("credit_card".equalsIgnoreCase(payment.getPaymentMethod()) || 
            "debit_card".equalsIgnoreCase(payment.getPaymentMethod())) {
            
            // Calculate processing fee: 2.9% + $0.30
            BigDecimal percentageFee = payment.getAmount().multiply(CARD_PROCESSING_RATE);
            BigDecimal totalFee = percentageFee.add(CARD_PROCESSING_FEE);
            
            payment.setProcessingFee(totalFee.setScale(2, RoundingMode.HALF_UP));
            payment.setNetAmount(payment.getAmount().subtract(totalFee).setScale(2, RoundingMode.HALF_UP));
        } else {
            // No processing fee for other payment methods in this mock
            payment.setProcessingFee(BigDecimal.ZERO);
            payment.setNetAmount(payment.getAmount());
        }
    }
    
    private void updatePaymentWithGatewayResult(Payment payment, PaymentGatewayService.PaymentGatewayResult result) {
        payment.setTransactionId(result.getTransactionId());
        payment.setGatewayTransactionId(result.getGatewayTransactionId());
        payment.setGatewayResponse(result.getGatewayResponse());
        payment.setReceiptUrl(result.getReceiptUrl());
        
        if (result.getStatus() == PaymentStatus.COMPLETED) {
            payment.markAsCompleted(result.getTransactionId());
        } else if (result.getStatus() == PaymentStatus.FAILED) {
            payment.markAsFailed(result.getFailureReason());
        } else {
            payment.setStatus(result.getStatus());
            payment.setProcessedAt(LocalDateTime.now());
        }
    }
    
    private PaymentResponseDto convertToResponseDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrderId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setGatewayTransactionId(payment.getGatewayTransactionId());
        dto.setFailureReason(payment.getFailureReason());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        
        return dto;
    }
    
    private PaymentDto convertResponseToLegacyDto(PaymentResponseDto response) {
        PaymentDto dto = new PaymentDto();
        dto.setId(response.getId());
        dto.setOrderId(response.getOrderId());
        dto.setAmount(response.getAmount());
        dto.setPaymentMethod(response.getPaymentMethod());
        dto.setStatus(response.getStatus());
        dto.setTransactionId(response.getTransactionId());
        dto.setFailureReason(response.getFailureReason());
        return dto;
    }
    
    // Additional service methods
    
    public List<PaymentResponseDto> getAllPaymentsEnhanced() {
        return paymentRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public Optional<PaymentResponseDto> getPaymentByIdEnhanced(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(this::convertToResponseDto);
    }
    
    public Optional<PaymentResponseDto> getPaymentByOrderIdEnhanced(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::convertToResponseDto);
    }
    
    public List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public PaymentResponseDto updatePaymentStatusEnhanced(Long paymentId, PaymentStatusUpdateDto updateDto) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found with id: " + paymentId));
        
        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(updateDto.getStatus());
        
        if (updateDto.getFailureReason() != null) {
            if (updateDto.getStatus() == PaymentStatus.FAILED) {
                payment.setFailureReason(updateDto.getFailureReason());
            }
        }
        
        if (updateDto.getGatewayTransactionId() != null) {
            payment.setGatewayTransactionId(updateDto.getGatewayTransactionId());
        }
        
        payment = paymentRepository.save(payment);
        
        logger.info("Payment {} status updated from {} to {}", paymentId, oldStatus, updateDto.getStatus());
        
        return convertToResponseDto(payment);
    }
    
    public PaymentResponseDto verifyPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found with id: " + paymentId));
        
        if (payment.getTransactionId() == null) {
            throw new PaymentProcessingException("Payment has no transaction ID to verify");
        }
        
        // Verify with payment gateway
        PaymentGatewayService.PaymentVerificationResult verificationResult = 
            paymentGatewayService.verifyPayment(payment.getTransactionId());
        
        // Update payment status if verification shows different status
        if (verificationResult.getStatus() != payment.getStatus()) {
            logger.info("Payment {} status verification: gateway says {}, local status is {}", 
                       paymentId, verificationResult.getStatus(), payment.getStatus());
            
            payment.setStatus(verificationResult.getStatus());
            payment.setGatewayResponse(verificationResult.getGatewayResponse());
            payment = paymentRepository.save(payment);
        }
        
        return convertToResponseDto(payment);
    }
}
