package me.sample.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

/**
 * DTO используемая для импорта партнеров через интеграционный эндпоинт
 * предоставленный банком
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartnerImportDTO {
    UUID id;
    String name;
    String title;
    List<String> phones;
    String site;
    List<UUID> shops;
    String link;

    @JsonProperty("icon_url")
    String iconUrl;
}
