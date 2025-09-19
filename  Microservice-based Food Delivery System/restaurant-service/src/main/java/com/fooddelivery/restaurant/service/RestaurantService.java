package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import com.fooddelivery.common.dto.RestaurantDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantService {
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    public List<RestaurantDto> getAllActiveRestaurants() {
        return restaurantRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<RestaurantDto> getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public RestaurantDto createRestaurant(RestaurantDto restaurantDto) {
        Restaurant restaurant = convertToEntity(restaurantDto);
        Restaurant saved = restaurantRepository.save(restaurant);
        return convertToDto(saved);
    }
    
    public RestaurantDto updateRestaurant(Long id, RestaurantDto restaurantDto) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        
        restaurant.setName(restaurantDto.getName());
        restaurant.setAddress(restaurantDto.getAddress());
        restaurant.setPhone(restaurantDto.getPhone());
        restaurant.setCuisine(restaurantDto.getCuisine());
        restaurant.setRating(restaurantDto.getRating());
        restaurant.setIsActive(restaurantDto.getIsActive());
        
        Restaurant updated = restaurantRepository.save(restaurant);
        return convertToDto(updated);
    }
    
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }
    
    public List<RestaurantDto> getRestaurantsByCuisine(String cuisine) {
        return restaurantRepository.findByCuisineAndIsActiveTrue(cuisine)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<RestaurantDto> searchRestaurantsByName(String name) {
        return restaurantRepository.findByNameContainingAndActive(name)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private RestaurantDto convertToDto(Restaurant restaurant) {
        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getCuisine(),
                restaurant.getDescription(),
                restaurant.getRating(),
                restaurant.getIsActive()
        );
    }
    
    private Restaurant convertToEntity(RestaurantDto dto) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        restaurant.setCuisine(dto.getCuisine());
        restaurant.setDescription(dto.getDescription());
        restaurant.setRating(dto.getRating()); // Allow null ratings for new restaurants
        restaurant.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return restaurant;
    }
}
