package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.Store;
import me.sample.domain.StoreWithClusterCounts;
import me.sample.domain.StoreWithClusters;
import me.sample.domain.geo.Cluster;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SecuredGeoAnalysisService {

    Long countClusters();

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
}
