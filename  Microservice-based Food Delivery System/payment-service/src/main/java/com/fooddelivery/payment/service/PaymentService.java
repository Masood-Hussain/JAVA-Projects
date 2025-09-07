package com.fooddelivery.payment.service;

import com.fooddelivery.payment.entity.Payment;
import com.fooddelivery.payment.repository.PaymentRepository;
import com.fooddelivery.common.dto.PaymentDto;
import com.fooddelivery.common.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public PaymentDto processPayment(PaymentDto paymentDto) {
        Payment payment = new Payment(
            paymentDto.getOrderId(),
            paymentDto.getAmount(),
            paymentDto.getPaymentMethod()
        );
        
        // Simulate payment processing
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setTransactionId(UUID.randomUUID().toString());
        
        // Simulate success/failure (90% success rate)
        boolean success = Math.random() > 0.1;
        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment declined by bank");
        }
        
        payment = paymentRepository.save(payment);
        return convertToDto(payment);
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
}
