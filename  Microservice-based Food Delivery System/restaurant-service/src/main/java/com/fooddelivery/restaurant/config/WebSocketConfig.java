package com.fooddelivery.restaurant.config;

import com.fooddelivery.restaurant.websocket.MenuAvailabilityWebSocketHandler;
import com.fooddelivery.restaurant.websocket.OrderTrackingWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private MenuAvailabilityWebSocketHandler menuHandler;

    @Autowired
    private OrderTrackingWebSocketHandler orderHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(menuHandler, "/ws/menu")
                .setAllowedOrigins("*");
                
        registry.addHandler(orderHandler, "/ws/orders")
                .setAllowedOrigins("*");
    }
}
