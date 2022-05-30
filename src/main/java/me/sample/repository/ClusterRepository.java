package me.sample.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import me.sample.domain.geo.Cluster;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ClusterRepository extends JpaRepository<Cluster, UUID>, JpaSpecificationExecutor<Cluster> {

    Set<Cluster> findAllByTerminalId(UUID terminalId);

    @EntityGraph(attributePaths = {"clusterGeopositions"})
    Optional<Cluster> findWithClusterGeopositionsById(UUID id);
}
