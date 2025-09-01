package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.entity.Delivery;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import com.fooddelivery.common.dto.DeliveryDto;
import com.fooddelivery.common.enums.DeliveryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = new Delivery(
            deliveryDto.getOrderId(),
            deliveryDto.getPickupAddress(),
            deliveryDto.getDeliveryAddress()
        );
        delivery = deliveryRepository.save(delivery);
        return convertToDto(delivery);
    }

    public List<DeliveryDto> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
            .map(this::convertToDto)
            .toList();
    }

    public Optional<DeliveryDto> getDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
            .map(this::convertToDto);
    }

    public DeliveryDto updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new RuntimeException("Delivery not found"));
        
        delivery.setStatus(status);
        if (status == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredTime(LocalDateTime.now());
        }
        
        delivery = deliveryRepository.save(delivery);
        return convertToDto(delivery);
    }

    private DeliveryDto convertToDto(Delivery delivery) {
        DeliveryDto dto = new DeliveryDto();
        dto.setId(delivery.getId());
        dto.setOrderId(delivery.getOrderId());
        dto.setDeliveryPersonId(delivery.getDeliveryPersonId());
        dto.setDeliveryPersonName(delivery.getDeliveryPersonName());
        dto.setPickupAddress(delivery.getPickupAddress());
        dto.setDeliveryAddress(delivery.getDeliveryAddress());
        dto.setStatus(delivery.getStatus());
        dto.setAssignedTime(delivery.getAssignedTime());
        dto.setDeliveredTime(delivery.getDeliveredTime());
        dto.setNotes(delivery.getNotes());
        return dto;
    }
}
