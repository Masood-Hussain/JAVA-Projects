package com.fooddelivery.order.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class OrderTrackingWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Store sessions by user ID and order ID
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> orderSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        String orderId = getOrderIdFromSession(session);
        
        if (userId != null) {
            userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        }
        
        if (orderId != null) {
            orderSessions.computeIfAbsent(orderId, k -> new CopyOnWriteArraySet<>()).add(session);
        }
        
        // Send welcome message with tracking capabilities
        sendWelcomeMessage(session, userId, orderId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            
            String type = (String) messageData.get("type");
            
            switch (type) {
                case "SUBSCRIBE_ORDER":
                    subscribeToOrder(session, (String) messageData.get("orderId"));
                    break;
                case "SUBSCRIBE_USER":
                    subscribeToUser(session, (String) messageData.get("userId"));
                    break;
                case "REQUEST_STATUS":
                    sendOrderStatus(session, (String) messageData.get("orderId"));
                    break;
                case "PING":
                    sendPong(session);
                    break;
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error: " + exception.getMessage());
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        removeSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // Public methods for broadcasting updates
    public void broadcastOrderUpdate(String orderId, Map<String, Object> update) {
        CopyOnWriteArraySet<WebSocketSession> sessions = orderSessions.get(orderId);
        if (sessions != null) {
            update.put("type", "ORDER_UPDATE");
            update.put("orderId", orderId);
            update.put("timestamp", System.currentTimeMillis());
            
            String message = serializeMessage(update);
            sessions.forEach(session -> sendMessage(session, message));
        }
    }

    public void broadcastDeliveryUpdate(String orderId, Map<String, Object> deliveryInfo) {
        CopyOnWriteArraySet<WebSocketSession> sessions = orderSessions.get(orderId);
        if (sessions != null) {
            deliveryInfo.put("type", "DELIVERY_UPDATE");
            deliveryInfo.put("orderId", orderId);
            deliveryInfo.put("timestamp", System.currentTimeMillis());
            
            String message = serializeMessage(deliveryInfo);
            sessions.forEach(session -> sendMessage(session, message));
        }
    }

    public void broadcastPaymentUpdate(String userId, String orderId, Map<String, Object> paymentInfo) {
        // Send to both user and order subscribers
        paymentInfo.put("type", "PAYMENT_UPDATE");
        paymentInfo.put("orderId", orderId);
        paymentInfo.put("timestamp", System.currentTimeMillis());
        
        String message = serializeMessage(paymentInfo);
        
        CopyOnWriteArraySet<WebSocketSession> userSess = userSessions.get(userId);
        if (userSess != null) {
            userSess.forEach(session -> sendMessage(session, message));
        }
        
        CopyOnWriteArraySet<WebSocketSession> orderSess = orderSessions.get(orderId);
        if (orderSess != null) {
            orderSess.forEach(session -> sendMessage(session, message));
        }
    }

    public void broadcastRestaurantUpdate(String restaurantId, Map<String, Object> restaurantInfo) {
        // Broadcast to all relevant sessions (could be optimized with restaurant-specific tracking)
        restaurantInfo.put("type", "RESTAURANT_UPDATE");
        restaurantInfo.put("restaurantId", restaurantId);
        restaurantInfo.put("timestamp", System.currentTimeMillis());
        
        String message = serializeMessage(restaurantInfo);
        
        // For now, broadcast to all active sessions (in production, would filter by restaurant)
        userSessions.values().forEach(sessions -> 
            sessions.forEach(session -> sendMessage(session, message)));
    }

    // Private helper methods
    private String getUserIdFromSession(WebSocketSession session) {
        // Extract from query parameters or headers
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            return extractParameter(query, "userId");
        }
        return null;
    }

    private String getOrderIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("orderId=")) {
            return extractParameter(query, "orderId");
        }
        return null;
    }

    private String extractParameter(String query, String param) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(param)) {
                return keyValue[1];
            }
        }
        return null;
    }

    private void sendWelcomeMessage(WebSocketSession session, String userId, String orderId) {
        Map<String, Object> welcome = Map.of(
            "type", "WELCOME",
            "message", "Connected to real-time tracking",
            "userId", userId != null ? userId : "anonymous",
            "orderId", orderId != null ? orderId : "none",
            "features", new String[]{"order_tracking", "delivery_updates", "payment_status", "restaurant_updates"}
        );
        
        sendMessage(session, serializeMessage(welcome));
    }

    private void subscribeToOrder(WebSocketSession session, String orderId) {
        orderSessions.computeIfAbsent(orderId, k -> new CopyOnWriteArraySet<>()).add(session);
        
        Map<String, Object> response = Map.of(
            "type", "SUBSCRIPTION_CONFIRMED",
            "orderId", orderId,
            "message", "Subscribed to order updates"
        );
        
        sendMessage(session, serializeMessage(response));
    }

    private void subscribeToUser(WebSocketSession session, String userId) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        
        Map<String, Object> response = Map.of(
            "type", "SUBSCRIPTION_CONFIRMED",
            "userId", userId,
            "message", "Subscribed to user updates"
        );
        
        sendMessage(session, serializeMessage(response));
    }

    private void sendOrderStatus(WebSocketSession session, String orderId) {
        // In real implementation, fetch from order service
        Map<String, Object> status = Map.of(
            "type", "ORDER_STATUS",
            "orderId", orderId,
            "status", "PREPARING",
            "estimatedTime", "25 minutes",
            "updates", new String[]{"Order confirmed", "Restaurant preparing", "Delivery assigned"}
        );
        
        sendMessage(session, serializeMessage(status));
    }

    private void sendPong(WebSocketSession session) {
        Map<String, Object> pong = Map.of("type", "PONG", "timestamp", System.currentTimeMillis());
        sendMessage(session, serializeMessage(pong));
    }

    private void removeSession(WebSocketSession session) {
        userSessions.values().forEach(sessions -> sessions.remove(session));
        orderSessions.values().forEach(sessions -> sessions.remove(session));
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (Exception e) {
            System.err.println("Failed to send WebSocket message: " + e.getMessage());
        }
    }

    private String serializeMessage(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{\"type\":\"ERROR\",\"message\":\"Serialization failed\"}";
        }
    }
}
