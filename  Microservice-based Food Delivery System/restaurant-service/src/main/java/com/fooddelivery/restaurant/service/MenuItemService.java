package com.fooddelivery.restaurant.service;

import com.fooddelivery.common.dto.MenuItemDto;
import com.fooddelivery.restaurant.entity.MenuItem;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuItemService {
    
    @Autowired
    private MenuItemRepository menuItemRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;

    public List<MenuItemDto> getAllMenuItems() {
        return menuItemRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MenuItemDto> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurant_Id(restaurantId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MenuItemDto> getAvailableMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurant_IdAndIsAvailable(restaurantId, true).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<MenuItemDto> getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .map(this::convertToDto);
    }

    public MenuItemDto createMenuItem(MenuItemDto menuItemDto) {
        MenuItem menuItem = convertToEntity(menuItemDto);
        MenuItem saved = menuItemRepository.save(menuItem);
        return convertToDto(saved);
    }

    public Optional<MenuItemDto> updateMenuItem(Long id, MenuItemDto menuItemDto) {
        return menuItemRepository.findById(id)
                .map(existingMenuItem -> {
                    existingMenuItem.setName(menuItemDto.getName());
                    existingMenuItem.setDescription(menuItemDto.getDescription());
                    existingMenuItem.setPrice(menuItemDto.getPrice());
                    existingMenuItem.setCategory(menuItemDto.getCategory());
                    existingMenuItem.setIsAvailable(menuItemDto.getIsAvailable());
                    MenuItem updated = menuItemRepository.save(existingMenuItem);
                    return convertToDto(updated);
                });
    }

    public boolean deleteMenuItem(Long id) {
        if (menuItemRepository.existsById(id)) {
            menuItemRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<MenuItemDto> getMenuItemsByRestaurantAndCategory(Long restaurantId, String category) {
        return menuItemRepository.findByRestaurant_IdAndCategoryAndIsAvailable(restaurantId, category, true)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<MenuItemDto> searchMenuItemsByName(Long restaurantId, String name) {
        return menuItemRepository.findByRestaurant_IdAndNameContainingIgnoreCaseAndIsAvailable(restaurantId, name, true)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public MenuItemDto toggleAvailability(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + menuItemId));
        
        menuItem.setIsAvailable(!menuItem.getIsAvailable());
        MenuItem updated = menuItemRepository.save(menuItem);
        return convertToDto(updated);
    }

    private MenuItemDto convertToDto(MenuItem menuItem) {
        MenuItemDto dto = new MenuItemDto();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        dto.setRestaurantId(menuItem.getRestaurantId());
        dto.setCategory(menuItem.getCategory());
        dto.setIsAvailable(menuItem.getIsAvailable());
        
        // Set advanced features
        dto.setImageUrl(menuItem.getImageUrl());
        if (menuItem.getIngredients() != null && !menuItem.getIngredients().isEmpty()) {
            dto.setIngredients(List.of(menuItem.getIngredients().split(",")));
        }
        dto.setPreparationTime(menuItem.getPreparationTime());
        dto.setSpiceLevel(menuItem.getSpiceLevel());
        dto.setCalories(menuItem.getCalories());
        dto.setVegetarian(menuItem.getVegetarian());
        dto.setVegan(menuItem.getVegan());
        dto.setGlutenFree(menuItem.getGlutenFree());
        dto.setHalal(menuItem.getHalal());
        dto.setKeto(menuItem.getKeto());
        if (menuItem.getAllergens() != null && !menuItem.getAllergens().isEmpty()) {
            dto.setAllergens(List.of(menuItem.getAllergens().split(",")));
        }
        dto.setRating(menuItem.getRating());
        dto.setReviewCount(menuItem.getReviewCount());
        dto.setCuisine(menuItem.getCuisine());
        dto.setFeatured(menuItem.getFeatured());
        dto.setPromotionalOffer(menuItem.getPromotionalOffer());
        
        return dto;
    }

    private MenuItem convertToEntity(MenuItemDto dto) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(dto.getId());
        menuItem.setName(dto.getName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice());
        menuItem.setCategory(dto.getCategory());
        
        // Properly associate with restaurant
        if (dto.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                    .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + dto.getRestaurantId()));
            menuItem.setRestaurant(restaurant);
        }
        
        // Set advanced features
        menuItem.setImageUrl(dto.getImageUrl());
        if (dto.getIngredients() != null) {
            menuItem.setIngredients(String.join(",", dto.getIngredients()));
        }
        menuItem.setPreparationTime(dto.getPreparationTime());
        menuItem.setSpiceLevel(dto.getSpiceLevel());
        menuItem.setCalories(dto.getCalories());
        menuItem.setVegetarian(dto.getVegetarian() != null ? dto.getVegetarian() : false);
        menuItem.setVegan(dto.getVegan() != null ? dto.getVegan() : false);
        menuItem.setGlutenFree(dto.getGlutenFree() != null ? dto.getGlutenFree() : false);
        menuItem.setHalal(dto.getHalal() != null ? dto.getHalal() : false);
        menuItem.setKeto(dto.getKeto() != null ? dto.getKeto() : false);
        if (dto.getAllergens() != null) {
            menuItem.setAllergens(String.join(",", dto.getAllergens()));
        }
        menuItem.setCuisine(dto.getCuisine());
        menuItem.setFeatured(dto.getFeatured() != null ? dto.getFeatured() : false);
        menuItem.setPromotionalOffer(dto.getPromotionalOffer());
        
        // Default to true if not provided (e.g., checkbox unchecked and no parameter sent)
        Boolean available = dto.getIsAvailable();
        if (available == null) {
            available = Boolean.TRUE;
        }
        menuItem.setIsAvailable(available);
        return menuItem;
    }
}
