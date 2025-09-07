package com.fooddelivery.payment.controller;

import com.fooddelivery.payment.service.PaymentService;
import com.fooddelivery.common.dto.PaymentDto;
import com.fooddelivery.common.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

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
}
