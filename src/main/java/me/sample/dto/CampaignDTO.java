package me.sample.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.domain.CampaignState;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CampaignDTO {

    UUID id;

    @NotNull
    CompanyDTO company;

    @NotBlank
    String name;

    String description;

    CampaignState status;

    Long radius;

    @JsonIgnore
    Long notificationLimit;

    @JsonIgnore
    Long notificationLimitPerTerminal;

    Integer delivered;
    Integer opened;
    Integer sent;
    Integer fail;

    @NotNull
    NotificationTemplateDTO notificationTemplate;

    @Valid
    DistributionDTO distribution;

    @Valid
    ScheduledGeoposDisseminationDTO scheduledGeoposDissemination;

    /**
     * Content-type списка id клиентов
     */
    String clientIdDataContentType;

    /**
     * Список id клиентов в base64
     */
    String clientIdDataBase64;

    /**
     * Кол-во клиентов. {@code null}, если список клиентов для рассылки не определен
     */
    Long clientIdCount;
}
