package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.Store;
import me.sample.domain.StoreWithClusterCounts;
import me.sample.domain.StoreWithClusters;
import me.sample.domain.Terminal;
import me.sample.domain.geo.Cluster;
import me.sample.domain.geo.ClusterGeoposition;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface GeoAnalysisService {

    Long countClusters(Specification<Cluster> specification);

    Page<Cluster> findClusters(Pageable pageable);

    Page<Cluster> findClusters(Specification<Cluster> specification, Pageable pageable);

    Set<Cluster> findClustersByTerminalId(UUID terminalId);

    StoreWithClusterCounts countStoresWithClusters(Long radius,
                                                   Specification<Store> storeSpecification,
                                                   Specification<Cluster> clusterSpecification);

    Page<StoreWithClusters> findStoresWithClusters(Long radius,
                                                   Specification<Store> storeSpecification,
                                                   Specification<Cluster> clusterSpecification,
                                                   Pageable pageable);

    Optional<StoreWithClusters> findStoreWithClusters(UUID storeId, Long radius,
                                                      Specification<Store> storeSpecification,
                                                      Specification<Cluster> clusterSpecification);

    List<ClusterGeoposition> clusterGeopositions(Collection<GeoPositionInfo> geopositions, Set<Cluster> clusters);

    ClusterGeoposition clusterGeoposition(GeoPositionInfo geoposition, Set<Cluster> clusters);

    void associateGeopositions();

    void associateGeopositionsForTerminal(UUID terminalId, int geopositionBatchSize);

    long associateGeopositionsForTerminal(Terminal terminal, int geopositionBatchSize);
}
