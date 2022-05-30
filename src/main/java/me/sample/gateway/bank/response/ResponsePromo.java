package me.sample.gateway.bank.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResponsePromo {

    /**
     * Id акции
     */
    UUID id;

    /**
     * Заголовок акции
     */
    String title;

    /**
     * Описание акции
     */
    String description;

    /**
     * Дата активации акции
     */
    LocalDate dateActivation;

    /**
     * Дата окончания акции
     */
    LocalDate dateDeactivation;

    /**
     * Флаг скрытой акции
     */
    Boolean isHide;

    /**
     * Наименование ссылки
     */
    String urlName;

    List<ResponseImage> images;
}
