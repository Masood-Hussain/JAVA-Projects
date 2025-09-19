package com.fooddelivery.restaurant.service;

import com.fooddelivery.common.dto.MenuItemDto;
import com.fooddelivery.restaurant.entity.MenuItem;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuItemService {
    
    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<MenuItemDto> getAllMenuItems() {
        return menuItemRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MenuItemDto> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MenuItemDto> getAvailableMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailable(restaurantId, true).stream()
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

    private MenuItemDto convertToDto(MenuItem menuItem) {
        return new MenuItemDto(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getRestaurantId(),
                menuItem.getCategory(),
                menuItem.getIsAvailable()
        );
    }

    private MenuItem convertToEntity(MenuItemDto dto) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(dto.getId());
        menuItem.setName(dto.getName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice());
        menuItem.setRestaurantId(dto.getRestaurantId());
        menuItem.setCategory(dto.getCategory());
        // Default to true if not provided (e.g., checkbox unchecked and no parameter sent)
        Boolean available = dto.getIsAvailable();
        if (available == null) {
            available = Boolean.TRUE;
        }
        menuItem.setIsAvailable(available);
        return menuItem;
    }
}
