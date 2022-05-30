package me.sample.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import me.sample.domain.Application;

import java.util.UUID;

/**
 * DTO сущности {@link Application}, которая отражает конкретное Android или iOS приложение,
 * которое интегрируется с системой для получения push-нотификаций
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationDTO {
    UUID id;
    String name;
    String apiKey;
    CompanyDTO company;
}
