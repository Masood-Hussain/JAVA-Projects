package com.fooddelivery.restaurant.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VirtualMenuItemDto {
    private Long itemId;
    private String itemName;
    private String description;
    private BigDecimal price;
    
    // 3D and AR visualization
    private String model3dUrl;
    private String arModelUrl;
    private List<String> imageUrls360;
    private String videoPreviewUrl;
    
    // Interactive features
    private Boolean hasVirtualTasting;
    private String virtualTastingUrl;
    private Boolean hasIngredientVisualization;
    private List<String> interactiveFeatures;
    
    // Customization options
    private List<Map<String, Object>> customizationOptions;
    private Boolean allowsVirtualCustomization;
    
    // Sensory data for enhanced experience
    private String aromaProfile;
    private String textureDescription;
    private String temperatureProfile;
    private List<String> soundEffects; // sizzling, crunchy, etc.
    
    // Device compatibility
    private List<String> supportedDevices; // AR glasses, smartphones, tablets
    private String minDeviceSpecs;
    private Boolean requiresSpecialHardware;
    
    // Social features
    private Boolean allowsVirtualSharing;
    private Boolean supportsMultiUserExperience;
    private String shareableArUrl;
    
    // Analytics
    private Integer virtualEngagementScore;
    private BigDecimal conversionRate; // virtual view to order
}