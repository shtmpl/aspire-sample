package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class StoreAnalysisDTO {

    StoreDTO store;

    Long clusterCount;

    Long clusterAcceptedGeopositionCount;

    Long terminalCount;

    List<ClusterDTO> clusters;
}
