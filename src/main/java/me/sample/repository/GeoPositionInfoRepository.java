package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import me.sample.domain.GeoPositionInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public interface GeoPositionInfoRepository extends JpaRepository<GeoPositionInfo, UUID>, JpaSpecificationExecutor<GeoPositionInfo> {

    List<GeoPositionInfo> findAllByTerminalIdOrderByCreatedDateAsc(UUID terminalId);

    @Query(
            value = "SELECT * " +
                    "FROM geo_pos_info " +
                    "WHERE geo_pos_info.terminal_id = :terminalId " +
                    "  AND geo_pos_info.clustered = FALSE " +
                    "LIMIT :limit",
            nativeQuery = true)
    List<GeoPositionInfo> findAllByTerminalIdAndClusteredFalse(@Param("terminalId") UUID terminalId,
                                                               @Param("limit") Integer limit);

    @Query(value = "select count(*) from (select terminal_id " +
            "from geo_pos_info " +
            "where earth_box(ll_to_earth(:lat, :lon), :radius) @> ll_to_earth(lat, lon) " +
            "  and cdat >= now() -  :days * interval '1 days'" +
            "group by terminal_id) as gpi", nativeQuery = true)
    Integer countTerminalsInRadius(Double lon, Double lat, Long radius, Integer days);

    @Query(
            value = "SELECT * FROM geo_pos_info " +
                    "WHERE earth_box(ll_to_earth(:lat, :lon), :radius) @> ll_to_earth(geo_pos_info.lat, geo_pos_info.lon)",
            nativeQuery = true)
    Stream<GeoPositionInfo> findAsStreamAllByGeopositionWithinRadius(@Param("lat") Double lat,
                                                                     @Param("lon") Double lon,
                                                                     @Param("radius") Long radius);

    @Query(
            value = "SELECT DISTINCT cast(terminal_id AS TEXT) " +
                    "FROM geo_pos_info " +
                    "WHERE geo_pos_info.clustered = FALSE",
            nativeQuery = true)
    Set<String> findDistinctTerminalIdsAsStringsByClusteredFalse();

    default Set<UUID> findDistinctTerminalIdsByClusteredFalse() {
        return findDistinctTerminalIdsAsStringsByClusteredFalse()
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    @Query(
            value = "SELECT DISTINCT cast(terminal_id AS TEXT) " +
                    "FROM geo_pos_info " +
                    "       INNER JOIN store " +
                    "                  ON earth_box(ll_to_earth(store.lat, store.lon), :radius) @> ll_to_earth(geo_pos_info.lat, geo_pos_info.lon) " +
                    "WHERE store.id IN :storeIds",
            nativeQuery = true)
    Set<String> findDistinctTerminalIdsAsStringsWithinRadiusOfStores(@Param("radius") Long radius,
                                                                     @Param("storeIds") Set<UUID> storeIds);

    default Set<UUID> findDistinctTerminalIdsWithinRadiusOfStores(Long radius, Set<UUID> storeIds) {
        return findDistinctTerminalIdsAsStringsWithinRadiusOfStores(radius, storeIds)
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    Optional<GeoPositionInfo> findFirstByTerminalIdOrderByCreatedDateDesc(UUID terminalId);
}
