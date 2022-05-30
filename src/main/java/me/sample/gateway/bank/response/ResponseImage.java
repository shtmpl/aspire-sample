package me.sample.gateway.bank.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResponseImage {

    /**
     * Тип изображения для сайта.
     * По умолчанию {@link ResponseImagePlatform#DESKTOP}
     */
    ResponseImagePlatform platform;

    /**
     * Тип изображения
     */
    ResponseImageType type;

    /**
     * Ссылка на изображение
     */
    String url;
}
