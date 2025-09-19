package com.fooddelivery.restaurant.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;
import java.util.List;

@Component
public class OrderTrackingWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> orderSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String orderId = getOrderIdFromSession(session);
        orderSessions.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(session);
        
        // Send initial order status
        sendInitialOrderStatus(session, orderId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String orderId = getOrderIdFromSession(session);
        List<WebSocketSession> sessions = orderSessions.get(orderId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                orderSessions.remove(orderId);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages from clients
        @SuppressWarnings("unused")
        String payload = message.getPayload();
        // Process tracking requests, etc.
    }

    public void broadcastOrderUpdate(String orderId, Map<String, Object> update) {
        List<WebSocketSession> sessions = orderSessions.get(orderId);
        if (sessions != null) {
            String updateJson;
            try {
                updateJson = objectMapper.writeValueAsString(update);
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(updateJson));
                    }
                }
            } catch (Exception e) {
                // Log error
            }
        }
    }

    private String getOrderIdFromSession(WebSocketSession session) {
        // Extract order ID from session attributes or query parameters
        if (session.getUri() != null) {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("orderId=")) {
                return query.substring(query.indexOf("orderId=") + 8).split("&")[0];
            }
        }
        return "default";
    }

    private void sendInitialOrderStatus(WebSocketSession session, String orderId) {
        try {
            Map<String, Object> initialData = Map.of(
                "type", "initial",
                "orderId", orderId,
                "message", "Connected to order tracking updates"
            );
            String json = objectMapper.writeValueAsString(initialData);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            // Log error
        }
    }
}
