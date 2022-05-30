package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.enumeration.Language;
import me.sample.enumeration.TemplateType;

import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NotificationTemplateDTO {

    UUID id;
    TemplateType type;
    Language language;
    String name;
    String subject;
    String contentType;
    String text;
    String customPushPartName;
    String customPushPartValue;

    @NotNull
    CompanyDTO company;

    /**
     * Список id ассоциированных рассылок
     */
    @Builder.Default
    List<UUID> disseminationIds = new LinkedList<>();
}
