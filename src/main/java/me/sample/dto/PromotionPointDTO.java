package me.sample.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionPointDTO {
    UUID id;
    UUID storeId;
    String name;
    String description;
    Boolean isFavorite;
    String shortDescription;
    String promoColor;
    String promoDescription;
    String pinIconUrl;
    String iconUrl;
    Double lat;
    Double lon;
    String address;
    String timePeriod;
    String imageUrl;
    String phone;
    String requirements;
    Integer discountPercent;

    CodeDTO code;
}

