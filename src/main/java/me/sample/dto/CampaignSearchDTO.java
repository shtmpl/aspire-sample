package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.domain.CampaignState;

import java.util.List;

/**
 * Объект инкапсулирует настройки фильтрации при
 * выдаче списка кампаний
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CampaignSearchDTO {

    /**
     * Поисковый запрос
     */
    String query;

    /**
     * Состояния (статусы)
     */
    List<CampaignState> states;
}
