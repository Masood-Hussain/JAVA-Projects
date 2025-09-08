package com.fooddelivery.common.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable RabbitMQ/Redis message broker for scalability
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest")
                .setSystemLogin("guest")
                .setSystemPasscode("guest")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setVirtualHost("/");
        
        // Application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // User-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Restaurant real-time updates
        registry.addEndpoint("/ws/restaurants")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Payment status updates
        registry.addEndpoint("/ws/payments")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Order tracking
        registry.addEndpoint("/ws/orders")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Delivery tracking
        registry.addEndpoint("/ws/deliveries")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Group ordering collaboration
        registry.addEndpoint("/ws/group-orders")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Real-time notifications
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
