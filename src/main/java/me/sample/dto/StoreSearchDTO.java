package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.domain.Source;
import me.sample.domain.StoreState;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Объект инкапсулирует настройки фильтрации при
 * выдаче списка торговых точек.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class StoreSearchDTO {

    /**
     * Поисковый запрос
     */
    String query;

    /**
     * Id партнеров
     */
    @Builder.Default
    List<UUID> partnerIds = new LinkedList<>();

    /**
     * Источники данных
     */
    @Builder.Default
    List<Source> sources = new LinkedList<>();

    /**
     * Состояния (статусы)
     */
    @Builder.Default
    List<StoreState> states = new LinkedList<>();
}
