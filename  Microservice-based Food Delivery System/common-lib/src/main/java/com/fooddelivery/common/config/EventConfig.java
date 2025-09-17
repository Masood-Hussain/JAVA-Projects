package com.fooddelivery.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Event configuration for the food delivery system
 * This is a simplified version without Kafka dependencies
 */
@Configuration
@EnableAsync
public class EventConfig {

    /**
     * Configuration for event-driven architecture
     * In a production environment, this would configure Kafka, RabbitMQ, or other message brokers
     */
    
    // Placeholder for future event system configuration
    // When Kafka dependencies are added, this class can be expanded to include:
    // - Producer configurations
    // - Consumer configurations  
    // - Topic management
    // - Serialization settings
    // - Error handling
}