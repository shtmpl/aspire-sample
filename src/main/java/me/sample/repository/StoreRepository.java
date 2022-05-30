package me.sample.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import me.sample.domain.Source;
import me.sample.domain.Store;

import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface StoreRepository extends JpaRepository<Store, UUID>, JpaSpecificationExecutor<Store> {

    Stream<Store> findAllBySource(Source source);

    Stream<Store> findAllByPartnerId(UUID partnerId);

    @Query(name = "StoreWithDistanceToGeoposition")
    List<Object[]> findAsRowsAllNeighbouringWithDistanceToGeoposition(@Param("lat") Double lat,
                                                                      @Param("lon") Double lon);

    default List<Map.Entry<Store, Double>> findAllNeighbouringWithDistanceToGeoposition(Double lat, Double lon) {
        return findAsRowsAllNeighbouringWithDistanceToGeoposition(lat, lon).stream()
                .map(this::mapToStoreWithDistanceToGeoposition)
                .collect(Collectors.toList());
    }

    default Map.Entry<Store, Double> mapToStoreWithDistanceToGeoposition(Object[] row) {
        return new AbstractMap.SimpleImmutableEntry<>((Store) row[0], (Double) row[1]);
    }

    @Query(value = "SELECT DISTINCT city FROM Store")
    List<String> findDistinctCities();

    @EntityGraph(attributePaths = {"promos"})
    Optional<Store> findWithPromosById(UUID id);
}
