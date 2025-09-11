package com.fooddelivery.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class SmartOrderRequestDto {
    private String userId;
    private String restaurantId;
    private List<SmartOrderItemDto> items;
    private String deliveryAddress;
    private String deliveryInstructions;
    private String paymentMethodId;
    private Boolean enableSmartOptimization;
    private String preferredDeliveryTime;
    private Boolean groupOrderOption;
    private String promoCode;
    private BigDecimal tipAmount;
    private Map<String, Object> preferences;
    private String deviceLocation;
    private String weatherCondition;
}

@Data
public class SmartOrderItemDto {
    private Long menuItemId;
    private Integer quantity;
    private List<String> customizations;
    private String specialInstructions;
    private Boolean substituteIfUnavailable;
}

@Data
public class SmartOrderResponseDto {
    private String orderId;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal estimatedDeliveryTime;
    private String trackingUrl;
    private List<SmartOptimizationDto> optimizations;
    private String qrCode;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}

@Data
public class SmartOptimizationDto {
    private String type; // ROUTE, PREPARATION, BUNDLING, TIMING
    private String description;
    private BigDecimal timeSaved; // in minutes
    private BigDecimal costSaved;
    private String aiConfidence;
}

@Data
public class OrderTrackingUpdateDto {
    private String orderId;
    private String status; // PLACED, CONFIRMED, PREPARING, READY, PICKED_UP, ON_THE_WAY, DELIVERED
    private String location;
    private Double latitude;
    private Double longitude;
    private String estimatedArrival;
    private String driverName;
    private String driverPhone;
    private String vehicleInfo;
    private LocalDateTime timestamp;
    private String message;
    private Map<String, Object> additionalInfo;
}

@Data
public class DeliveryETADto {
    private String orderId;
    private Integer estimatedMinutes;
    private String estimatedArrivalTime;
    private String accuracy; // HIGH, MEDIUM, LOW
    private List<String> factors; // Traffic, weather, distance, etc.
    private String aiModel;
    private Map<String, Object> realTimeData;
}

@Data
public class ScheduledOrderRequestDto {
    private String userId;
    private String restaurantId;
    private List<SmartOrderItemDto> items;
    private String deliveryAddress;
    private LocalDateTime scheduledTime;
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY
    private String paymentMethodId;
    private Boolean sendReminder;
    private String timeZone;
}

@Data
public class ScheduledOrderResponseDto {
    private String scheduledOrderId;
    private String orderId;
    private String status;
    private LocalDateTime scheduledTime;
    private String recurrencePattern;
    private LocalDateTime nextOccurrence;
    private Boolean isActive;
}

@Data
public class GroupOrderRequestDto {
    private String initiatorUserId;
    private String restaurantId;
    private String deliveryAddress;
    private List<String> participantEmails;
    private LocalDateTime orderDeadline;
    private BigDecimal maxParticipants;
    private String paymentSplitType; // EQUAL, BY_ITEM, CUSTOM
    private String groupName;
}

@Data
public class GroupOrderResponseDto {
    private String groupOrderId;
    private String status;
    private String shareUrl;
    private String qrCode;
    private List<GroupParticipantDto> participants;
    private BigDecimal totalAmount;
    private LocalDateTime orderDeadline;
}

@Data
public class GroupParticipantDto {
    private String userId;
    private String email;
    private String name;
    private String status; // INVITED, JOINED, ORDERED, PAID
    private BigDecimal orderAmount;
    private LocalDateTime joinedAt;
}

@Data
public class SubscriptionOrderRequestDto {
    private String userId;
    private String restaurantId;
    private List<SmartOrderItemDto> items;
    private String deliveryAddress;
    private String frequency; // DAILY, WEEKLY, MONTHLY
    private String deliveryDays; // MON,WED,FRI
    private String deliveryTime;
    private String paymentMethodId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

@Data
public class SubscriptionOrderResponseDto {
    private String subscriptionId;
    private String status;
    private String frequency;
    private LocalDateTime nextDelivery;
    private BigDecimal recurringAmount;
    private String paymentStatus;
    private Boolean isActive;
}

@Data
public class OrderCancellationRequestDto {
    private String reason;
    private String detailedReason;
    private Boolean requestRefund;
    private String preferredRefundMethod;
}

@Data
public class OrderCancellationDto {
    private String orderId;
    private String cancellationId;
    private String status;
    private BigDecimal refundAmount;
    private String refundMethod;
    private String estimatedRefundTime;
    private LocalDateTime cancelledAt;
}

@Data
public class OrderAnalyticsDto {
    private String userId;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private String favoriteRestaurant;
    private String favoriteCategory;
    private String preferredOrderTime;
    private Map<String, Integer> orderFrequency;
    private List<String> recommendations;
    private Map<String, Object> insights;
}

@Data
public class VoiceOrderRequestDto {
    private String userId;
    private String audioUrl;
    private String transcription;
    private String language;
    private String deliveryAddress;
}

@Data
public class VoiceOrderResponseDto {
    private String orderId;
    private String transcription;
    private String interpretation;
    private SmartOrderResponseDto orderDetails;
    private String confidence;
    private List<String> clarificationQuestions;
}

@Data
public class AROrderPreviewDto {
    private String orderId;
    private String arModelUrl;
    private List<ARFoodItemDto> items;
    private String nutritionalVisualization;
    private String portionSizeReference;
}

@Data
public class ARFoodItemDto {
    private String itemName;
    private String ar3DModelUrl;
    private String textureUrl;
    private String nutritionalOverlay;
    private Map<String, Object> dimensions;
}

@Data
public class SmartOrderRecommendationDto {
    private String recommendationType; // CONTEXT, WEATHER, TIME, PREFERENCE
    private String title;
    private String description;
    private List<SmartOrderItemDto> suggestedItems;
    private String restaurantId;
    private String restaurantName;
    private BigDecimal estimatedPrice;
    private String reasoning;
    private String confidence;
}
