package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.FieldDefaults;
import me.sample.domain.PartnerState;
import me.sample.domain.Source;

import java.util.List;
import java.util.UUID;

/**
 * Объект инкапсулирует настройки фильтрации при
 * выдаче списка партнеров.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PartnerSearchDTO {

    @Singular
    List<UUID> ids;

    /**
     * Источники данных
     */
    @Singular
    List<Source> sources;

    /**
     * Список статусов для фильтрации
     */
    @Singular
    List<PartnerState> states;

    /**
     * Поисковый запрос
     */
    String query;
}
