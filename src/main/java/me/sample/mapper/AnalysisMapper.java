package me.sample.mapper;

import me.sample.dto.ClusterDTO;
import me.sample.dto.GeoPositionInfoDTO;
import me.sample.dto.StoreAnalysisCountDTO;
import me.sample.dto.StoreAnalysisDTO;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.StoreWithClusterCounts;
import me.sample.domain.StoreWithClusters;
import me.sample.domain.Terminal;
import me.sample.domain.geo.Cluster;
import me.sample.domain.geo.ClusterGeoposition;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnalysisMapper {

    ClusterDTO toDto(Cluster cluster);

    @Overview
    @Mappings({
            @Mapping(target = "terminal", ignore = true)
    })
    ClusterDTO toOverviewDto(Cluster cluster);

    @Mappings({
            @Mapping(target = "id", source = "geoposition.id"),
            @Mapping(target = "createdDate", source = "geoposition.createdDate"),
            @Mapping(target = "lat", source = "geoposition.lat"),
            @Mapping(target = "lon", source = "geoposition.lon")
    })
    GeoPositionInfoDTO toDto(ClusterGeoposition clusterGeoposition);

    @AfterMapping
    default void sortClusterAcceptedGeopositions(@MappingTarget ClusterDTO result) {
        if (result.getAcceptedGeopositions() == null) {
            return;
        }

        result.getAcceptedGeopositions().sort(Comparator.comparing(GeoPositionInfoDTO::getCreatedDate));
    }

    @AfterMapping
    default void sortClusterRejectedGeopositions(@MappingTarget ClusterDTO result) {
        if (result.getRejectedGeopositions() == null) {
            return;
        }

        result.getRejectedGeopositions().sort(Comparator.comparing(GeoPositionInfoDTO::getCreatedDate));
    }

    @Mappings({
            @Mapping(target = "clusters", ignore = true)
    })
    StoreAnalysisDTO toOverviewDto(StoreWithClusters storeWithClusters);

    @AfterMapping
    default void setCounts(@MappingTarget StoreAnalysisDTO result,
                           StoreWithClusters storeWithClusters) {
        if (storeWithClusters == null) {
            return;
        }

        Set<Cluster> clusters = storeWithClusters.getClusters();
        if (clusters == null) {
            return;
        }

        result.setClusterCount((long) clusters.size());

        Set<GeoPositionInfo> clusterAcceptedGeopositions = clusters.stream()
                .map(Cluster::getAcceptedGeopositions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        result.setClusterAcceptedGeopositionCount((long) clusterAcceptedGeopositions.size());

        Set<Terminal> clusterTerminals = clusters.stream()
                .map(Cluster::getTerminal)
                .collect(Collectors.toSet());

        result.setTerminalCount((long) clusterTerminals.size());
    }

    @Detailed
    StoreAnalysisDTO toDetailedDto(StoreWithClusters storeWithClusters);


    StoreAnalysisCountDTO toDto(StoreWithClusterCounts counts);
}
