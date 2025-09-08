package com.fooddelivery.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.time.Duration;

@Configuration
public class AdvancedGatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Advanced Restaurant Service Routes with AI-powered load balancing
            .route("restaurant-service-v2", r -> r
                .path("/api/v2/restaurants/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("restaurant-circuit-breaker")
                        .setFallbackUri("forward:/fallback/restaurant"))
                    .retry(config -> config
                        .setRetries(3)
                        .setMethods(org.springframework.http.HttpMethod.GET)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, false))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .addRequestHeader("X-Gateway-Version", "2.0")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}")
                    .rewritePath("/api/v2/restaurants/(?<segment>.*)", "/api/restaurants/${segment}"))
                .uri("lb://restaurant-service"))
            
            // Advanced Payment Service Routes with fraud detection
            .route("payment-service-v2", r -> r
                .path("/api/v2/payments/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("payment-circuit-breaker")
                        .setFallbackUri("forward:/fallback/payment"))
                    .retry(config -> config
                        .setRetries(2)
                        .setMethods(org.springframework.http.HttpMethod.POST)
                        .setBackoff(Duration.ofMillis(200), Duration.ofMillis(2000), 2, false))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .addRequestHeader("X-Fraud-Check", "enabled")
                    .addRequestHeader("X-Payment-Gateway", "advanced")
                    .rewritePath("/api/v2/payments/(?<segment>.*)", "/api/payments/${segment}"))
                .uri("lb://payment-service"))
            
            // Order Service with real-time tracking
            .route("order-service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("order-circuit-breaker")
                        .setFallbackUri("forward:/fallback/order"))
                    .retry(config -> config.setRetries(3))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .addRequestHeader("X-Real-Time-Tracking", "enabled"))
                .uri("lb://order-service"))
            
            // Delivery Service with AI route optimization
            .route("delivery-service", r -> r
                .path("/api/deliveries/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("delivery-circuit-breaker")
                        .setFallbackUri("forward:/fallback/delivery"))
                    .retry(config -> config.setRetries(2))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .addRequestHeader("X-Route-Optimization", "ai-enabled"))
                .uri("lb://delivery-service"))
            
            // WebSocket routes for real-time features
            .route("websocket-notifications", r -> r
                .path("/ws/**")
                .uri("lb://notification-service"))
            
            // AI Service routes for recommendations and analytics
            .route("ai-service", r -> r
                .path("/api/ai/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("ai-circuit-breaker")
                        .setFallbackUri("forward:/fallback/ai"))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .addRequestHeader("X-AI-Processing", "enabled"))
                .uri("lb://ai-service"))
            
            // Analytics and reporting service
            .route("analytics-service", r -> r
                .path("/api/analytics/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("analytics-circuit-breaker"))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(adminKeyResolver()))
                    .addRequestHeader("X-Analytics-Version", "v2"))
                .uri("lb://analytics-service"))
            
            .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOriginPattern("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> exchange.getRequest().getHeaders().getFirst("X-User-ID") != null ?
            reactor.core.publisher.Mono.just(exchange.getRequest().getHeaders().getFirst("X-User-ID")) :
            reactor.core.publisher.Mono.just("anonymous");
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver adminKeyResolver() {
        return exchange -> exchange.getRequest().getHeaders().getFirst("X-Admin-ID") != null ?
            reactor.core.publisher.Mono.just(exchange.getRequest().getHeaders().getFirst("X-Admin-ID")) :
            reactor.core.publisher.Mono.just("admin-default");
    }
}
