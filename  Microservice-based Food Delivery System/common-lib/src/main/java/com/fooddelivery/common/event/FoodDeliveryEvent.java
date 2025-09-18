package com.fooddelivery.common.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base event class for food delivery system events
 * This is a simplified version without Lombok dependencies
 */
public class FoodDeliveryEvent {
    
    private String eventId;
    private String eventType;
    private String source;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
    private String version;
    
    // Default constructor
    public FoodDeliveryEvent() {
        this.timestamp = LocalDateTime.now();
        this.version = "1.0";
    }
    
    // Parameterized constructor
    public FoodDeliveryEvent(String eventId, String eventType, String source, Map<String, Object> data) {
        this();
        this.eventId = eventId;
        this.eventType = eventType;
        this.source = source;
        this.data = data;
    }
    
    // Getters and setters
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}
