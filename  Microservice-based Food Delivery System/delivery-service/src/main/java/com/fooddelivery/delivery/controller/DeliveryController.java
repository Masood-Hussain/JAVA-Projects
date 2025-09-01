package com.fooddelivery.delivery.controller;

import com.fooddelivery.delivery.service.DeliveryService;
import com.fooddelivery.common.dto.DeliveryDto;
import com.fooddelivery.common.enums.DeliveryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryDto> createDelivery(@RequestBody DeliveryDto deliveryDto) {
        DeliveryDto created = deliveryService.createDelivery(deliveryDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryDto>> getAllDeliveries() {
        List<DeliveryDto> deliveries = deliveryService.getAllDeliveries();
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryDto> getDeliveryByOrderId(@PathVariable Long orderId) {
        return deliveryService.getDeliveryByOrderId(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<DeliveryDto> updateDeliveryStatus(
            @PathVariable Long deliveryId, 
            @RequestParam DeliveryStatus status) {
        DeliveryDto updated = deliveryService.updateDeliveryStatus(deliveryId, status);
        return ResponseEntity.ok(updated);
    }
}
