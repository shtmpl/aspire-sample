package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import me.sample.domain.geo.ClusterGeoposition;
import me.sample.domain.geo.ClusterGeopositionId;

import java.util.List;
import java.util.UUID;

public interface ClusterGeopositionRepository extends JpaRepository<ClusterGeoposition, ClusterGeopositionId> {

    List<ClusterGeoposition> findAllByClusterId(UUID clusterId);
}
