package com.fooddelivery.order.controller;

import com.fooddelivery.order.service.AdvancedOrderService;
import com.fooddelivery.order.dto.*;
import com.fooddelivery.common.dto.OrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/orders")
@CrossOrigin(origins = "*")
public class AdvancedOrderController {

    @Autowired
    private AdvancedOrderService advancedOrderService;

    /**
     * Create order with AI-powered optimization
     */
    @PostMapping("/create")
    public Mono<ResponseEntity<SmartOrderResponseDto>> createSmartOrder(
            @RequestBody SmartOrderRequestDto orderRequest) {
        return advancedOrderService.createSmartOrder(orderRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Real-time order tracking stream
     */
    @GetMapping("/{orderId}/tracking")
    public Flux<OrderTrackingUpdateDto> getOrderTrackingStream(@PathVariable String orderId) {
        return advancedOrderService.getOrderTrackingStream(orderId);
    }

    /**
     * Get AI-powered delivery time estimates
     */
    @GetMapping("/{orderId}/eta")
    public Mono<ResponseEntity<DeliveryETADto>> getDeliveryETA(@PathVariable String orderId) {
        return advancedOrderService.getAIDeliveryETA(orderId)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Schedule order for future delivery
     */
    @PostMapping("/schedule")
    public Mono<ResponseEntity<ScheduledOrderResponseDto>> scheduleOrder(
            @RequestBody ScheduledOrderRequestDto scheduleRequest) {
        return advancedOrderService.scheduleOrder(scheduleRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Group order for multiple users
     */
    @PostMapping("/group")
    public Mono<ResponseEntity<GroupOrderResponseDto>> createGroupOrder(
            @RequestBody GroupOrderRequestDto groupRequest) {
        return advancedOrderService.createGroupOrder(groupRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Order with subscription/recurring delivery
     */
    @PostMapping("/subscription")
    public Mono<ResponseEntity<SubscriptionOrderResponseDto>> createSubscriptionOrder(
            @RequestBody SubscriptionOrderRequestDto subscriptionRequest) {
        return advancedOrderService.createSubscriptionOrder(subscriptionRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Quick reorder from order history
     */
    @PostMapping("/{orderId}/reorder")
    public Mono<ResponseEntity<SmartOrderResponseDto>> reorderWithAdjustments(
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, Object> adjustments) {
        return advancedOrderService.reorderWithAdjustments(orderId, adjustments)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Cancel order with intelligent refund processing
     */
    @PostMapping("/{orderId}/cancel")
    public Mono<ResponseEntity<OrderCancellationDto>> cancelOrderSmart(
            @PathVariable String orderId,
            @RequestBody OrderCancellationRequestDto cancellationRequest) {
        return advancedOrderService.cancelOrderSmart(orderId, cancellationRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get order analytics and insights
     */
    @GetMapping("/analytics/{userId}")
    public Mono<ResponseEntity<OrderAnalyticsDto>> getOrderAnalytics(
            @PathVariable String userId,
            @RequestParam(defaultValue = "30") int days) {
        return advancedOrderService.getOrderAnalytics(userId, days)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Voice order processing
     */
    @PostMapping("/voice")
    public Mono<ResponseEntity<VoiceOrderResponseDto>> processVoiceOrder(
            @RequestBody VoiceOrderRequestDto voiceRequest) {
        return advancedOrderService.processVoiceOrder(voiceRequest)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * AR/VR order visualization
     */
    @GetMapping("/{orderId}/ar-preview")
    public Mono<ResponseEntity<AROrderPreviewDto>> getAROrderPreview(@PathVariable String orderId) {
        return advancedOrderService.generateAROrderPreview(orderId)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Smart order recommendations based on context
     */
    @GetMapping("/recommendations")
    public Flux<SmartOrderRecommendationDto> getSmartOrderRecommendations(
            @RequestParam String userId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String weather,
            @RequestParam(required = false) String timeOfDay) {
        return advancedOrderService.getContextualOrderRecommendations(userId, location, weather, timeOfDay);
    }
}
