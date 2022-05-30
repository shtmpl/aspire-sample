package me.sample.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeoPositionInfoDTO {

    UUID id;

    Double lon;
    Double lat;
    LocalDateTime createdDate;
    LocalDateTime terminalUpdatedDate;
    LocalDateTime terminalCreatedDate;
    String pushToken;
    String model;
    String vendor;
    String terminalId;
    Map<String, Object> props;
}
