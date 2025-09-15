package com.fooddelivery.restaurant.dto;

import com.fooddelivery.common.dto.MenuItemDto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuRecommendationDto {
    private MenuItemDto menuItem;
    private BigDecimal recommendationScore;
    private String recommendationReason;
    private String aiConfidence;
    private String[] tags;
    private Boolean isPersonalized;
    private String matchingCriteria;
}
