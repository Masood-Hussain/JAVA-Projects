package com.fooddelivery.frontend.service;

import com.fooddelivery.common.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
public class FrontendService {

    private final WebClient webClient;
    
    @Value("${api.gateway.url:http://localhost:8080}")
    private String apiGatewayUrl;

    public FrontendService() {
        this.webClient = WebClient.builder().build();
    }

    public List<RestaurantDto> getAllRestaurants() {
        try {
            RestaurantDto[] restaurants = webClient.get()
                .uri("http://localhost:8081/api/restaurants")
                .retrieve()
                .bodyToMono(RestaurantDto[].class)
                .block();
            return Arrays.asList(restaurants != null ? restaurants : new RestaurantDto[0]);
        } catch (Exception e) {
            return Arrays.asList();
        }
    }

    public RestaurantDto getRestaurantById(Long id) {
        try {
            return webClient.get()
                .uri("http://localhost:8081/api/restaurants/" + id)
                .retrieve()
                .bodyToMono(RestaurantDto.class)
                .block();
        } catch (Exception e) {
            return null;
        }
    }

    public RestaurantDto createRestaurant(RestaurantDto restaurant) {
        try {
            // Set default values if not provided
            if (restaurant.getRating() == null) {
                restaurant.setRating(0.0);
            }
            if (restaurant.getIsActive() == null) {
                restaurant.setIsActive(true);
            }
            
            return webClient.post()
                .uri("http://localhost:8081/api/restaurants")
                .header("Content-Type", "application/json")
                .bodyValue(restaurant)
                .retrieve()
                .bodyToMono(RestaurantDto.class)
                .block();
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            throw new RuntimeException("Could not create restaurant: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Could not create restaurant: " + e.getMessage());
        }
    }

    public RestaurantDto updateRestaurant(RestaurantDto restaurant) {
        try {
            // Ensure required fields have default values
            if (restaurant.getRating() == null) {
                restaurant.setRating(0.0);
            }
            if (restaurant.getIsActive() == null) {
                restaurant.setIsActive(true);
            }
            
            return webClient.put()
                .uri("http://localhost:8081/api/restaurants/" + restaurant.getId())
                .bodyValue(restaurant)
                .retrieve()
                .bodyToMono(RestaurantDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Could not update restaurant: " + e.getMessage());
        }
    }

    public void deleteRestaurant(Long id) {
        try {
            webClient.delete()
                .uri("http://localhost:8081/api/restaurants/" + id)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Could not delete restaurant: " + e.getMessage());
        }
    }

    public List<OrderDto> getAllOrders() {
        try {
            OrderDto[] orders = webClient.get()
                .uri("http://localhost:8082/api/orders")
                .retrieve()
                .bodyToMono(OrderDto[].class)
                .block();
            return Arrays.asList(orders != null ? orders : new OrderDto[0]);
        } catch (Exception e) {
            return Arrays.asList();
        }
    }

    public OrderDto getOrderById(Long id) {
        try {
            return webClient.get()
                .uri("http://localhost:8082/api/orders/" + id)
                .retrieve()
                .bodyToMono(OrderDto.class)
                .block();
        } catch (Exception e) {
            return null;
        }
    }

    public OrderDto createOrder(OrderDto order) {
        try {
            return webClient.post()
                .uri("http://localhost:8082/api/orders")
                .bodyValue(order)
                .retrieve()
                .bodyToMono(OrderDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Could not create order: " + e.getMessage());
        }
    }

    public List<DeliveryDto> getAllDeliveries() {
        try {
            DeliveryDto[] deliveries = webClient.get()
                .uri("http://localhost:8083/api/deliveries")
                .retrieve()
                .bodyToMono(DeliveryDto[].class)
                .block();
            return Arrays.asList(deliveries != null ? deliveries : new DeliveryDto[0]);
        } catch (Exception e) {
            return Arrays.asList();
        }
    }

    public List<PaymentDto> getAllPayments() {
        try {
            PaymentDto[] payments = webClient.get()
                .uri("http://localhost:8084/api/payments")
                .retrieve()
                .bodyToMono(PaymentDto[].class)
                .block();
            return Arrays.asList(payments != null ? payments : new PaymentDto[0]);
        } catch (Exception e) {
            return Arrays.asList();
        }
    }

    // Menu Item Management Methods
    public RestaurantDto getRestaurant(Long restaurantId) {
        try {
            return webClient.get()
                .uri("http://localhost:8081/api/restaurants/" + restaurantId)
                .retrieve()
                .bodyToMono(RestaurantDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Could not get restaurant: " + e.getMessage());
        }
    }

    public List<MenuItemDto> getMenuItems(Long restaurantId) {
        try {
            MenuItemDto[] menuItems = webClient.get()
                .uri("http://localhost:8081/api/restaurants/" + restaurantId + "/menu")
                .retrieve()
                .bodyToMono(MenuItemDto[].class)
                .block();
            return Arrays.asList(menuItems != null ? menuItems : new MenuItemDto[0]);
        } catch (Exception e) {
            return Arrays.asList();
        }
    }

    public MenuItemDto createMenuItem(Long restaurantId, MenuItemDto menuItem) {
        try {
            return webClient.post()
                .uri("http://localhost:8081/api/restaurants/" + restaurantId + "/menu")
                .bodyValue(menuItem)
                .retrieve()
                .bodyToMono(MenuItemDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Could not create menu item: " + e.getMessage());
        }
    }

    public MenuItemDto updateMenuItem(Long restaurantId, Long itemId, MenuItemDto menuItem) {
        try {
            return webClient.put()
                .uri("http://localhost:8081/api/restaurants/" + restaurantId + "/menu/" + itemId)
                .bodyValue(menuItem)
                .retrieve()
                .bodyToMono(MenuItemDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Could not update menu item: " + e.getMessage());
        }
    }

    public void deleteMenuItem(Long restaurantId, Long itemId) {
        try {
            webClient.delete()
                .uri("http://localhost:8081/api/restaurants/" + restaurantId + "/menu/" + itemId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Could not delete menu item: " + e.getMessage());
        }
    }
}
