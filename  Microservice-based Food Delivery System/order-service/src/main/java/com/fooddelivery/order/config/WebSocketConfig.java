package com.fooddelivery.order.config;

import com.fooddelivery.order.websocket.OrderTrackingWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private OrderTrackingWebSocketHandler orderTrackingHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderTrackingHandler, "/ws/orders")
                .setAllowedOrigins("*") // Configure properly for production
                .withSockJS(); // Fallback for browsers that don't support WebSocket
        
        registry.addHandler(orderTrackingHandler, "/ws/tracking")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
