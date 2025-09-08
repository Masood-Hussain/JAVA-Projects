package com.fooddelivery.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

@Configuration
public class Enhanced2025GatewayConfig {

    @Bean
    public RouteLocator enhanced2025RouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Enhanced Restaurant Service Routes with AI and Real-time features
            .route("restaurant-advanced-menu", r -> r
                .path("/api/v2/restaurants/**")
                .filters(f -> f
                    .addRequestHeader("X-Gateway-Version", "2025-Enhanced")
                    .addRequestHeader("X-Request-ID", "${T(java.util.UUID).randomUUID().toString()}")
                    .addRequestHeader("X-Feature-Set", "AI,REALTIME,ANALYTICS")
                    .circuitBreaker(c -> c
                        .setName("restaurant-advanced-cb")
                        .setFallbackUri("forward:/fallback/restaurant-advanced")
                        .setSlowCallDurationThreshold(java.time.Duration.ofSeconds(2))
                        .setSlowCallRateThreshold(50.0f)
                        .setFailureRateThreshold(30.0f))
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofSeconds(1), 2, false))
                    .requestRateLimiter(rl -> rl
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(intelligentKeyResolver()))
                )
                .uri("lb://restaurant-service"))
                
            // Advanced Payment Service with Multi-Gateway Support
            .route("payment-advanced", r -> r
                .path("/api/v2/payments/**")
                .filters(f -> f
                    .addRequestHeader("X-Gateway-Version", "2025-Payment")
                    .addRequestHeader("X-Request-ID", "${T(java.util.UUID).randomUUID().toString()}")
                    .addRequestHeader("X-Payment-Features", "CRYPTO,BNPL,SPLIT,FRAUD_AI")
                    .circuitBreaker(c -> c
                        .setName("payment-advanced-cb")
                        .setFallbackUri("forward:/fallback/payment-advanced")
                        .setSlowCallDurationThreshold(java.time.Duration.ofSeconds(5))
                        .setFailureRateThreshold(20.0f))
                    .retry(retryConfig -> retryConfig
                        .setRetries(2)
                        .setBackoff(java.time.Duration.ofMillis(200), java.time.Duration.ofSeconds(2), 2, false))
                    .requestRateLimiter(rl -> rl
                        .setRateLimiter(paymentRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                )
                .uri("lb://payment-service"))
                
            // Real-time WebSocket Routes for Live Updates
            .route("restaurant-realtime", r -> r
                .path("/ws/restaurants/**")
                .filters(f -> f
                    .addRequestHeader("X-WebSocket-Type", "RESTAURANT_UPDATES")
                    .addRequestHeader("X-Real-Time", "true"))
                .uri("lb:ws://restaurant-service"))
                
            .route("payment-realtime", r -> r
                .path("/ws/payments/**")
                .filters(f -> f
                    .addRequestHeader("X-WebSocket-Type", "PAYMENT_STATUS")
                    .addRequestHeader("X-Real-Time", "true"))
                .uri("lb:ws://payment-service"))
                
            .route("order-realtime", r -> r
                .path("/ws/orders/**")
                .filters(f -> f
                    .addRequestHeader("X-WebSocket-Type", "ORDER_TRACKING")
                    .addRequestHeader("X-Real-Time", "true"))
                .uri("lb:ws://order-service"))
                
            // AI and Machine Learning Service Routes
            .route("ai-recommendations", r -> r
                .path("/api/ai/**")
                .filters(f -> f
                    .addRequestHeader("X-Service-Type", "AI_ML")
                    .addRequestHeader("X-AI-Version", "2025")
                    .circuitBreaker(c -> c
                        .setName("ai-ml-cb")
                        .setFallbackUri("forward:/fallback/ai-basic")
                        .setSlowCallDurationThreshold(java.time.Duration.ofSeconds(10)))
                    .requestRateLimiter(rl -> rl
                        .setRateLimiter(aiRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                )
                .uri("lb://ai-service"))
                
            // Analytics and Business Intelligence Routes
            .route("analytics-service", r -> r
                .path("/api/analytics/**")
                .filters(f -> f
                    .addRequestHeader("X-Service-Type", "ANALYTICS")
                    .addRequestHeader("X-Analytics-Version", "2025")
                    .circuitBreaker(c -> c.setName("analytics-cb"))
                )
                .uri("lb://analytics-service"))
                
            // Enhanced Order Service with Smart Features
            .route("order-service-enhanced", r -> r
                .path("/api/v2/orders/**")
                .filters(f -> f
                    .addRequestHeader("X-Order-Features", "SMART_ROUTING,PREDICTIVE_DELIVERY")
                    .addRequestHeader("X-Request-ID", "${T(java.util.UUID).randomUUID().toString()}")
                    .circuitBreaker(c -> c
                        .setName("order-enhanced-cb")
                        .setFallbackUri("forward:/fallback/order-basic"))
                )
                .uri("lb://order-service"))
                
            // Advanced Delivery Service with IoT Integration
            .route("delivery-service-enhanced", r -> r
                .path("/api/v2/deliveries/**")
                .filters(f -> f
                    .addRequestHeader("X-Delivery-Features", "IOT,GPS_TRACKING,DRONE_SUPPORT")
                    .addRequestHeader("X-Request-ID", "${T(java.util.UUID).randomUUID().toString()}")
                    .circuitBreaker(c -> c
                        .setName("delivery-enhanced-cb")
                        .setFallbackUri("forward:/fallback/delivery-basic"))
                )
                .uri("lb://delivery-service"))
                
            // Backward Compatibility Routes
            .route("restaurant-legacy", r -> r
                .path("/api/restaurants/**")
                .and().method(HttpMethod.GET)
                .filters(f -> f
                    .addRequestHeader("X-Gateway-Version", "Legacy")
                    .addRequestHeader("X-Compatibility-Mode", "v1"))
                .uri("lb://restaurant-service"))
                
            .route("payment-legacy", r -> r
                .path("/api/payments/**")
                .and().method(HttpMethod.GET, HttpMethod.POST)
                .filters(f -> f
                    .addRequestHeader("X-Gateway-Version", "Legacy")
                    .addRequestHeader("X-Compatibility-Mode", "v1"))
                .uri("lb://payment-service"))
                
            // Frontend and Static Content
            .route("frontend-enhanced", r -> r
                .path("/", "/app/**", "/static/**", "/assets/**", "/manifest.json")
                .filters(f -> f
                    .addRequestHeader("X-Frontend-Version", "2025-PWA")
                    .addResponseHeader("Cache-Control", "public, max-age=31536000"))
                .uri("lb://frontend-service"))
                
            .build();
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // Standard rate limiter: 100 requests per second with burst capacity of 200
        return new RedisRateLimiter(100, 200, 1);
    }

    @Bean
    public RedisRateLimiter paymentRateLimiter() {
        // More restrictive for payments: 20 requests per second with burst of 50
        return new RedisRateLimiter(20, 50, 1);
    }

    @Bean
    public RedisRateLimiter aiRateLimiter() {
        // AI service rate limiter: 10 requests per second with burst of 20
        return new RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            String key = userId != null ? "user:" + userId : 
                        apiKey != null ? "api:" + apiKey : 
                        "anonymous:" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(key);
        };
    }

    @Bean
    public KeyResolver intelligentKeyResolver() {
        return exchange -> {
            // Intelligent key resolution based on multiple factors
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-ID");
            String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
            
            if (userId != null) {
                return Mono.just("user:" + userId);
            } else if (clientId != null) {
                return Mono.just("client:" + clientId);
            } else if (userAgent != null && userAgent.contains("Mobile")) {
                return Mono.just("mobile:" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
            } else {
                return Mono.just("web:" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
            }
        };
    }
}
