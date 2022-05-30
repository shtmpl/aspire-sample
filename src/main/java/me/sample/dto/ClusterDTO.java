package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
public class ClusterDTO {

    UUID id;

    Double lat;

    Double lon;

    Integer visitCount;

    LocalDateTime lastVisitedAt;

    TerminalDTO terminal;

    List<GeoPositionInfoDTO> acceptedGeopositions;

    List<GeoPositionInfoDTO> rejectedGeopositions;
}
