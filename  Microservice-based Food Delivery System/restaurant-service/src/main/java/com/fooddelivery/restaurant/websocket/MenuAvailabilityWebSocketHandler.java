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
public class MenuAvailabilityWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> restaurantSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String restaurantId = getRestaurantIdFromSession(session);
        restaurantSessions.computeIfAbsent(restaurantId, k -> new CopyOnWriteArrayList<>()).add(session);
        
        // Send initial menu availability
        sendInitialMenuAvailability(session, restaurantId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String restaurantId = getRestaurantIdFromSession(session);
        List<WebSocketSession> sessions = restaurantSessions.get(restaurantId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                restaurantSessions.remove(restaurantId);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages from clients (e.g., subscription requests)
        String payload = message.getPayload();
        // Process subscription requests, filters, etc.
    }

    public void broadcastMenuUpdate(String restaurantId, Map<String, Object> update) {
        List<WebSocketSession> sessions = restaurantSessions.get(restaurantId);
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

    private String getRestaurantIdFromSession(WebSocketSession session) {
        // Extract restaurant ID from session attributes or query parameters
        String query = session.getUri().getQuery();
        if (query != null && query.contains("restaurantId=")) {
            return query.substring(query.indexOf("restaurantId=") + 13).split("&")[0];
        }
        return "default";
    }

    private void sendInitialMenuAvailability(WebSocketSession session, String restaurantId) {
        try {
            Map<String, Object> initialData = Map.of(
                "type", "initial",
                "restaurantId", restaurantId,
                "message", "Connected to menu availability updates"
            );
            String json = objectMapper.writeValueAsString(initialData);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            // Log error
        }
    }
}
