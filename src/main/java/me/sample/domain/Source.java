package me.sample.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Источник, на основании данных которого была создана сущность
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum Source {

    /**
     * Создание через фронтенд (web эндпоинты)
     */
    LOCAL(Values.LOCAL),

    /**
     * Создание на основе интеграционных данных от банка
     */
    IMPORT_BANK(Values.IMPORT_BANK);

    String value;

    public static class Values {

        public static final String LOCAL = "LOCAL";
        public static final String IMPORT_BANK = "IMPORT_BANK";
    }
}
