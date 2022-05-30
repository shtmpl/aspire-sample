package me.sample.dto;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import me.sample.domain.TerminalPlatform;

import java.util.Map;
import java.util.UUID;

@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TerminalDTO {
    UUID id;

    TerminalPlatform platform;
    String appBundle;
    String vendor;
    String model;
    String hardwareId;
    String osVersion;
    String appVersion;

    String msisdn;
    String pushId;

    String ip;

    Boolean test;

    @Singular
    Map<String, Object> props;

    @ToString.Exclude
    ApplicationDTO application;
}
