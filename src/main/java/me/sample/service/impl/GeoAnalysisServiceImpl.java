package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.GeoAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Store;
import me.sample.domain.StoreSpecifications;
import me.sample.domain.StoreWithClusterCounts;
import me.sample.domain.StoreWithClusters;
import me.sample.domain.Terminal;
import me.sample.domain.Analysis;
import me.sample.domain.geo.Cluster;
import me.sample.domain.geo.ClusterGeoposition;
import me.sample.domain.geo.ClusterSpecifications;
import me.sample.domain.geo.Event;
import me.sample.domain.geo.EventDistance;
import me.sample.domain.geo.Point;
import me.sample.repository.ClusterGeopositionRepository;
import me.sample.repository.ClusterRepository;
import me.sample.repository.GeoPositionInfoRepository;
import me.sample.repository.StoreRepository;
import me.sample.repository.TerminalRepository;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class GeoAnalysisServiceImpl implements GeoAnalysisService {

    private static final double THRESHOLD_SPATIAL_DISTANCE_IN_METERS = 200;
    private static final long THRESHOLD_TEMPORAL_DISTANCE_IN_MINUTES = 30;

    private static final int BATCH_SIZE_GEOPOSITION_ASSOCIATION = 1000;


    @NonFinal
    GeoAnalysisService self;

    TerminalRepository terminalRepository;

    ClusterRepository clusterRepository;
    GeoPositionInfoRepository geoPositionInfoRepository;
    ClusterGeopositionRepository clusterGeopositionRepository;

    StoreRepository storeRepository;

    @Autowired
    public void setSelf(@Lazy GeoAnalysisService self) {
        this.self = self;
    }

    @Transactional(readOnly = true)
    @Override
    public Long countClusters(Specification<Cluster> specification) {
        return clusterRepository.count(specification);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Cluster> findClusters(Pageable pageable) {
        return clusterRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Cluster> findClusters(Specification<Cluster> specification, Pageable pageable) {
        return clusterRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Set<Cluster> findClustersByTerminalId(UUID terminalId) {
        return clusterRepository.findAllByTerminalId(terminalId);
    }

    @Override
    public StoreWithClusterCounts countStoresWithClusters(Long radius,
                                                          Specification<Store> storeSpecification,
                                                          Specification<Cluster> clusterSpecification) {
        List<Store> stores = storeRepository.findAll(storeSpecification);

        Set<Cluster> distinctClusters = stores
                .stream()
                .map((Store store) ->
                        clusterRepository.findAll(
                                ClusterSpecifications.coordinatesWithinRadius(store.getLat(), store.getLon(), radius)))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<GeoPositionInfo> distinctAcceptedGeopositions = distinctClusters
                .stream()
                .map(Cluster::getAcceptedGeopositions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Set<Terminal> distinctTerminals = distinctClusters
                .stream()
                .map(Cluster::getTerminal)
                .collect(Collectors.toSet());

        return StoreWithClusterCounts.builder()
                .storeCount((long) stores.size())
                .uniqueClusterCount((long) distinctClusters.size())
                .uniqueClusterAcceptedGeopositionCount((long) distinctAcceptedGeopositions.size())
                .uniqueTerminalCount((long) distinctTerminals.size())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StoreWithClusters> findStoresWithClusters(Long radius,
                                                          Specification<Store> storeSpecification,
                                                          Specification<Cluster> clusterSpecification,
                                                          Pageable pageable) {
        Page<Store> stores = storeRepository.findAll(
                Specification.where(storeSpecification),
                pageable);

        return stores
                .map((Store store) -> {
                    List<Cluster> clusters = clusterRepository.findAll(
                            Specification.where(clusterSpecification)
                                    .and(ClusterSpecifications.coordinatesWithinRadius(store.getLat(), store.getLon(), radius)));

                    return StoreWithClusters.builder()
                            .store(store)
                            .clusters(new LinkedHashSet<>(clusters))
                            .build();
                });
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StoreWithClusters> findStoreWithClusters(UUID storeId, Long radius,
                                                             Specification<Store> storeSpecification,
                                                             Specification<Cluster> clusterSpecification) {
        Store found = storeRepository.findOne(
                Specification.where(storeSpecification)
                        .and(StoreSpecifications.idEqualTo(storeId)))
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        List<Cluster> clusters = clusterRepository.findAll(
                Specification.where(clusterSpecification)
                        .and(ClusterSpecifications.coordinatesWithinRadius(found.getLat(), found.getLon(), radius)));

        return Optional.of(StoreWithClusters.builder()
                .store(found)
                .clusters(new LinkedHashSet<>(clusters))
                .build());
    }

    @Transactional(readOnly = true)
    public List<ClusterGeoposition> clusterGeopositions(Collection<GeoPositionInfo> geopositions, Set<Cluster> clusters) {
        Set<Cluster> existingClusters = new LinkedHashSet<>(clusters);
        List<ClusterGeoposition> result = new LinkedList<>();
        for (GeoPositionInfo geoposition : geopositions.stream()
                .sorted(Comparator.comparing(GeoPositionInfo::getCreatedDate))
                .collect(Collectors.toList())) {
            ClusterGeoposition clusterGeoposition = clusterGeoposition(geoposition, existingClusters);

            existingClusters.add(clusterGeoposition.getCluster());
            result.add(clusterGeoposition);
        }

        return result;
    }

    /**
     * <pre>
     * Описание алгоритма определения "мест":
     * - Достаем из базы список координат терминала в хронологическом порядке;
     * - Первую координату определяем как новое место - место1 фиксируем в специальном объекте время, геопозицию, количество посещений = 1;
     * - Далее каждую следующую координату сопоставляем с теми местами что уже сформированы:
     *   а) если следующая координата находится в пределах 200 метров (если несколько - берется ближайшее по расстоянию)
     *      от одного из найденных ранее мест, и временной интервал менее 30 минут,
     *      то считаем это шумом и добавляем в список проигнорированных точек.
     *   б) если следующая координата находится в пределах 200 метров от одного из найденных ранее мест(если несколько - берется ближайшее по расстоянию), но времени прошло более 30 минут,
     *      то считаем что пользователь посетил это "место" второй раз > увеличиваем счетчик посещений этого места, обновляем время последнего посещения места, и далее новые координаты и время сравниваем с этими.
     *   в) если следующая координата находится более чем в 200 метрах от всех ранее обнаруженных "мест",
     *      то "фиксируем" новое место, и используем его для дальнейших вычислений
     * </pre>
     */
    @Transactional(readOnly = true)
    public ClusterGeoposition clusterGeoposition(GeoPositionInfo geoposition, Set<Cluster> clusters) {
        if (clusters.isEmpty()) {
            log.debug("({}, {}, {}) => New Cluster. Reason: No prior clusters provided",
                    geoposition.getLat(), geoposition.getLon(), geoposition.getCreatedDate());

            return ClusterGeoposition.builder()
                    .cluster(Cluster.builder()
                            .lat(geoposition.getLat())
                            .lon(geoposition.getLon())
                            .lastVisitedAt(geoposition.getCreatedDate())
                            .visitCount(1)
                            .build())
                    .geoposition(geoposition)
                    .accepted(true)
                    .build();
        }

        Map<Cluster, EventDistance> eventDistancesByCluster = clusters.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        (Cluster cluster) -> EventDistance.builder()
                                .spatialDistance(Analysis.spatialDistance(toPoint(geoposition), toPoint(cluster)))
                                .temporalDistance(Analysis.temporalDistance(toEvent(geoposition), toEvent(cluster)))
                                .build()));

        Map.Entry<Cluster, EventDistance> nearestClusterWithEventDistance = eventDistancesByCluster.entrySet().stream()
                .min(Comparator.comparing((Map.Entry<Cluster, EventDistance> it) -> it.getValue().getSpatialDistance())
                        .thenComparing((Map.Entry<Cluster, EventDistance> it) -> it.getValue().getTemporalDistance()))
                .orElse(null);

        if (nearestClusterWithEventDistance == null ||
                nearestClusterWithEventDistance.getValue().getSpatialDistanceValueInMeters() > THRESHOLD_SPATIAL_DISTANCE_IN_METERS) {
            log.debug("({}, {}, {}) => New Cluster. Reason: No prior clusters within geo distance threshold: {}",
                    geoposition.getLat(), geoposition.getLon(), geoposition.getCreatedDate(),
                    THRESHOLD_SPATIAL_DISTANCE_IN_METERS);

            return ClusterGeoposition.builder()
                    .cluster(Cluster.builder()
                            .lat(geoposition.getLat())
                            .lon(geoposition.getLon())
                            .lastVisitedAt(geoposition.getCreatedDate())
                            .visitCount(1)
                            .build())
                    .geoposition(geoposition)
                    .accepted(true)
                    .build();
        }

        Cluster cluster = nearestClusterWithEventDistance.getKey();

        if (nearestClusterWithEventDistance.getValue().getSpatialDistanceValueInMeters() <= THRESHOLD_SPATIAL_DISTANCE_IN_METERS &&
                nearestClusterWithEventDistance.getValue().getTemporalDistanceValueInMinutes() <= THRESHOLD_TEMPORAL_DISTANCE_IN_MINUTES) {
            log.debug("({}, {}, {}) => Noise. Reason: Found cluster w/ geoDistance: {} <= {}, timeDistance: {} <= {}",
                    geoposition.getLat(), geoposition.getLon(), geoposition.getCreatedDate(),
                    nearestClusterWithEventDistance.getValue().getSpatialDistanceValueInMeters(), THRESHOLD_SPATIAL_DISTANCE_IN_METERS,
                    nearestClusterWithEventDistance.getValue().getTemporalDistanceValueInMinutes(), THRESHOLD_TEMPORAL_DISTANCE_IN_MINUTES);

            return ClusterGeoposition.builder()
                    .cluster(cluster)
                    .geoposition(geoposition)
                    .accepted(false)
                    .build();
        }

        log.debug("({}, {}, {}) => Cluster. Reason: Found cluster w/ geoDistance: {} <= {}, timeDistance: {} > {}",
                geoposition.getLat(), geoposition.getLon(), geoposition.getCreatedDate(),
                nearestClusterWithEventDistance.getValue().getSpatialDistanceValueInMeters(), THRESHOLD_SPATIAL_DISTANCE_IN_METERS,
                nearestClusterWithEventDistance.getValue().getTemporalDistanceValueInMinutes(), THRESHOLD_TEMPORAL_DISTANCE_IN_MINUTES);

        return ClusterGeoposition.builder()
                .cluster(cluster
                        .setLastVisitedAt(geoposition.getCreatedDate())
                        .setVisitCount(cluster.getVisitCount() + 1))
                .geoposition(geoposition)
                .accepted(true)
                .build();
    }

    private static Event toEvent(Cluster cluster) {
        return Event.builder()
                .at(cluster.getLastVisitedAt())
                .point(toPoint(cluster))
                .build();
    }

    private static Point toPoint(Cluster cluster) {
        return Point.builder()
                .lat(cluster.getLat())
                .lon(cluster.getLon())
                .build();
    }

    private static Event toEvent(GeoPositionInfo geoposition) {
        return Event.builder()
                .at(geoposition.getCreatedDate())
                .point(toPoint(geoposition))
                .build();
    }

    private static Point toPoint(GeoPositionInfo geoposition) {
        return Point.builder()
                .lat(geoposition.getLat())
                .lon(geoposition.getLon())
                .build();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void associateGeopositions() {
        log.debug(".associateGeopositions()");

        Set<UUID> terminalIds = geoPositionInfoRepository.findDistinctTerminalIdsByClusteredFalse();
        log.info("Found {} terminals with non-clustered geoposition(s)", terminalIds.size());

        terminalIds.forEach((UUID terminalId) -> {
            associateGeopositionsForTerminal(terminalId, BATCH_SIZE_GEOPOSITION_ASSOCIATION);
        });
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void associateGeopositionsForTerminal(UUID terminalId, int geopositionBatchSize) {
        log.debug(".associateGeopositions(Terminal.id: {})", terminalId);

        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new NotFoundResourceException("Terminal", terminalId));

        long savedOrUpdatedClusterCount;
        while ((savedOrUpdatedClusterCount = self.associateGeopositionsForTerminal(terminal, geopositionBatchSize)) > 0) {
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public long associateGeopositionsForTerminal(Terminal terminal, int geopositionBatchSize) {
        List<GeoPositionInfo> nonClusteredGeopositions = geoPositionInfoRepository.findAllByTerminalIdAndClusteredFalse(terminal.getId(), geopositionBatchSize);
        if (nonClusteredGeopositions.isEmpty()) {
            return 0;
        }

        log.info("Associating {} geopositions for terminal id: {}...", nonClusteredGeopositions.size(), terminal.getId());

        Set<Cluster> clusters = clusterRepository.findAllByTerminalId(terminal.getId());
        List<ClusterGeoposition> clusterGeopositions = clusterGeopositions(nonClusteredGeopositions, clusters);

        clusterRepository.saveAll(clusterGeopositions
                .stream()
                .map(ClusterGeoposition::getCluster)
                .map((Cluster cluster) -> cluster.setTerminal(terminal))
                .collect(Collectors.toSet()));

        geoPositionInfoRepository.saveAll(clusterGeopositions
                .stream()
                .map(ClusterGeoposition::getGeoposition)
                .map((GeoPositionInfo geoposition) -> geoposition.setClustered(true))
                .collect(Collectors.toSet()));

        return (long) clusterGeopositionRepository.saveAll(clusterGeopositions).size();
    }
}
