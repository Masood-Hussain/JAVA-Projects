package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    List<Restaurant> findByIsActiveTrue();
    
    List<Restaurant> findByCuisineAndIsActiveTrue(String cuisine);
    
    @Query("SELECT r FROM Restaurant r WHERE r.name LIKE %:name% AND r.isActive = true")
    List<Restaurant> findByNameContainingAndActive(@Param("name") String name);
    
    @Query("SELECT r FROM Restaurant r WHERE r.rating >= :rating AND r.isActive = true ORDER BY r.rating DESC")
    List<Restaurant> findByRatingGreaterThanEqualAndActive(@Param("rating") Double rating);
}
