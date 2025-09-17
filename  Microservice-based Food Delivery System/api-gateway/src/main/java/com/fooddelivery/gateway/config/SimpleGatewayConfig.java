package com.fooddelivery.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleGatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Restaurant Service
            .route("restaurant-service", r -> r
                .path("/api/restaurants/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("restaurant-circuit-breaker")
                        .setFallbackUri("forward:/fallback/restaurant"))
                    .retry(config -> config.setRetries(3)))
                .uri("lb://restaurant-service"))
                
            // Order Service
            .route("order-service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("order-circuit-breaker")
                        .setFallbackUri("forward:/fallback/order"))
                    .retry(config -> config.setRetries(3)))
                .uri("lb://order-service"))
                
            // Payment Service
            .route("payment-service", r -> r
                .path("/api/payments/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("payment-circuit-breaker")
                        .setFallbackUri("forward:/fallback/payment"))
                    .retry(config -> config.setRetries(3)))
                .uri("lb://payment-service"))
                
            // Delivery Service
            .route("delivery-service", r -> r
                .path("/api/deliveries/**", "/api/v1/delivery/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("delivery-circuit-breaker")
                        .setFallbackUri("forward:/fallback/delivery"))
                    .retry(config -> config.setRetries(3)))
                .uri("lb://delivery-service"))
                
            // Frontend Service
            .route("frontend-service", r -> r
                .path("/", "/app/**", "/static/**")
                .uri("lb://frontend-service"))
                
            .build();
    }
}