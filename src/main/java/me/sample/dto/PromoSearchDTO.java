package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.FieldDefaults;
import me.sample.domain.PromoState;
import me.sample.domain.Source;

import java.util.LinkedList;
import java.util.List;

/**
 * Объект инкапсулирует настройки фильтрации при
 * выдаче списка акций.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PromoSearchDTO {

    /**
     * Поисковый запрос
     */
    String query;

    /**
     * Источники данных
     */
    @Singular
    List<Source> sources = new LinkedList<>();

    /**
     * Список статусов для фильтрации
     */
    @Singular
    List<PromoState> states = new LinkedList<>();
}
