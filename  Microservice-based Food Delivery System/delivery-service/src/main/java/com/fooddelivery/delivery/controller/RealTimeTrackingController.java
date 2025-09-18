package com.fooddelivery.delivery.controller;

import com.fooddelivery.delivery.service.DeliveryService;
import com.fooddelivery.common.dto.DeliveryDto;
import com.fooddelivery.common.enums.DeliveryStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/delivery")
@CrossOrigin(origins = "*")
public class RealTimeTrackingController {

    private final DeliveryService deliveryService;

    public RealTimeTrackingController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * Basic order tracking by order ID
     */
    @GetMapping("/track/order/{orderId}")
    public ResponseEntity<DeliveryDto> trackOrderByOrderId(@PathVariable Long orderId) {
        Optional<DeliveryDto> delivery = deliveryService.getDeliveryByOrderId(orderId);
        return delivery.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get delivery status by delivery ID
     */
    @GetMapping("/track/{deliveryId}")
    public ResponseEntity<DeliveryDto> getDeliveryStatus(@PathVariable Long deliveryId) {
        Optional<DeliveryDto> delivery = deliveryService.getDeliveryById(deliveryId);
        return delivery.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update delivery status
     */
    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<DeliveryDto> updateDeliveryStatus(
            @PathVariable Long deliveryId, 
            @RequestParam DeliveryStatus status) {
        try {
            DeliveryDto updated = deliveryService.updateDeliveryStatus(deliveryId, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all deliveries for a delivery person
     */
    @GetMapping("/person/{deliveryPersonId}")
    public ResponseEntity<List<DeliveryDto>> getDeliveriesForPerson(
            @PathVariable Long deliveryPersonId) {
        List<DeliveryDto> deliveries = deliveryService.getDeliveriesByDeliveryPersonId(deliveryPersonId);
        return ResponseEntity.ok(deliveries);
    }
}