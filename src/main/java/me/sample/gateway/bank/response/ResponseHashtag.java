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
public class ResponseHashtag {

    /**
     * Отображаемое наименование хэштэга
     */
    String key;

    /**
     * Описание хэштэга
     */
    String description;
}
