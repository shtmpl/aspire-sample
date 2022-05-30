package me.sample.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.SecuredGeoAnalysisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import me.sample.config.SwaggerConfiguration;
import me.sample.dto.ClusterDTO;
import me.sample.dto.AnalysisDTO;
import me.sample.dto.StoreAnalysisCountDTO;
import me.sample.dto.StoreAnalysisDTO;
import me.sample.mapper.AnalysisMapper;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Store;
import me.sample.domain.StoreSpecifications;
import me.sample.domain.StoreWithClusterCounts;
import me.sample.domain.StoreWithClusters;
import me.sample.domain.geo.ClusterSpecifications;
import me.sample.domain.PropFilter;
import me.sample.domain.Specifications;
import me.sample.domain.geo.Cluster;
import me.sample.web.rest.util.PaginationUtil;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Api(tags = SwaggerConfiguration.ANALYSIS)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
@RequestMapping("/api/analysis")
@RestController
public class AnalysisResource {

    SecuredGeoAnalysisService securedGeoAnalysisService;

    AnalysisMapper analysisMapper;

    @GetMapping("/terminal/geoposition-cluster/count")
    public ResponseEntity<Long> countIndexedClusters() {
        Long count = securedGeoAnalysisService.countClusters();

        return ResponseEntity.ok(count);
    }

    @ApiOperation(value = "Кластеризация геопозиций терминалов")
    @GetMapping("/terminal/geoposition-cluster")
    public ResponseEntity<List<ClusterDTO>> indexClusters(@SortDefault("terminal.id") Pageable pageable) {
        Page<Cluster> clusters = securedGeoAnalysisService.findClusters(pageable);
        Page<ClusterDTO> result = clusters.map(analysisMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/terminal/geoposition-cluster"))
                .body(result.getContent());
    }

    @ApiOperation(value = "Кластеризация геопозиций терминала")
    @GetMapping("/terminal/{terminalId}/geoposition-cluster")
    public ResponseEntity<List<ClusterDTO>> indexClusters(@PathVariable UUID terminalId) {
        Set<Cluster> clusters = securedGeoAnalysisService.findClustersByTerminalId(terminalId);
        List<ClusterDTO> results = clusters.stream()
                .map(analysisMapper::toDto)
                .sorted(Comparator.comparing(ClusterDTO::getLastVisitedAt).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    @ApiOperation(value = "Возвращает набор частых мест (без ассоциации с терминалами) с учетом фильтра")
    @PostMapping("/cluster")
    public ResponseEntity<List<ClusterDTO>> analyzeClusters(@Valid @RequestBody AnalysisDTO request) {
        List<StoreWithClusters> storeWithClusters = securedGeoAnalysisService.findStoresWithClusters(
                request.getRadius(),
                makeStoreSpecification(request),
                makeClusterSpecification(request),
                Pageable.unpaged())
                .getContent();
        List<ClusterDTO> result = storeWithClusters.stream()
                .map(StoreWithClusters::getClusters)
                .flatMap(Collection::stream)
                .distinct()
                .map(analysisMapper::toOverviewDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Возвращает (постраничный) список торговых точек с учетом фильтра. " +
            "Для каждой торговой точки указывается: " +
            "количество частых мест в окрестностях, " +
            "количество терминалов этих частых мест и " +
            "количество геолокаций (кроме \"шумовых\") из геокластеров попадающих под фильтр")
    @PostMapping("/store")
    public ResponseEntity<List<StoreAnalysisDTO>> analyzeStores(@Valid @RequestBody AnalysisDTO request,
                                                                @SortDefault("id") Pageable pageable) {
        Page<StoreWithClusters> storeWithClusters = securedGeoAnalysisService.findStoresWithClusters(
                request.getRadius(),
                makeStoreSpecification(request),
                makeClusterSpecification(request),
                pageable);
        Page<StoreAnalysisDTO> result = storeWithClusters.map(analysisMapper::toOverviewDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/store"))
                .body(result.getContent());
    }

    @ApiOperation(value = "Возвращает кол-во торговых точек с учетом фильтра. " +
            "Также указывается: " +
            "количество уникальных частых мест в окрестностях всех торговых точек, удовлетворяющих фильтру, " +
            "количество уникальных терминалов среди всех торговых точек, удовлетворяющих фильтру," +
            "количество уникальных геолокаций (кроме \"шумовых\") из геокластеров попадающих под фильтр")
    @PostMapping("/store/count")
    public ResponseEntity<StoreAnalysisCountDTO> countAnalyzedStores(@Valid @RequestBody AnalysisDTO request) {
        StoreWithClusterCounts counts = securedGeoAnalysisService.countStoresWithClusters(
                request.getRadius(),
                makeStoreSpecification(request),
                makeClusterSpecification(request));
        StoreAnalysisCountDTO result = analysisMapper.toDto(counts);

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Возвращает детальную информацию по торговой точке: " +
            "список ассоциированных частых мест, " +
            "их терминалов и т.д. " +
            "с учетом фильтра")
    @PostMapping("/store/{id}")
    public ResponseEntity<StoreAnalysisDTO> analyzeStore(@PathVariable UUID id,
                                                         @Valid @RequestBody AnalysisDTO request) {
        StoreWithClusters storeWithClusters = securedGeoAnalysisService.findStoreWithClusters(
                id, request.getRadius(),
                makeStoreSpecification(request),
                makeClusterSpecification(request))
                .orElseThrow(() -> new NotFoundResourceException("Store", id));
        StoreAnalysisDTO result = analysisMapper.toDetailedDto(storeWithClusters);

        return ResponseEntity.ok(result);
    }

    private Specification<Store> makeStoreSpecification(AnalysisDTO request) {
        Specification<Store> result = Specifications.any();

        List<UUID> storePartnerIds = request.getStorePartnerIds();
        if (storePartnerIds != null && !storePartnerIds.isEmpty()) {
            result = result.and(StoreSpecifications.partnerIdIn(storePartnerIds));
        }

        List<String> storeCities = request.getStoreCities();
        if (storeCities != null && !storeCities.isEmpty()) {
            Specification<Store> storeCitySpecification = Specifications.none();
            for (String city : storeCities) {
                storeCitySpecification = storeCitySpecification.or(StoreSpecifications.cityLike(city));
            }

            result = result.and(storeCitySpecification);
        }

        return result;
    }

    private Specification<Cluster> makeClusterSpecification(AnalysisDTO request) {
        Specification<Cluster> result = Specifications.any();

        List<PropFilter> clusterVisitCountFilters = request.getClusterVisitCountFilters();
        if (clusterVisitCountFilters != null && !clusterVisitCountFilters.isEmpty()) {
            for (PropFilter filter : clusterVisitCountFilters) {
                result = result.and(ClusterSpecifications.visitCountMatches(filter));
            }
        }

        List<PropFilter> terminalFilters = request.getTerminalFilters();
        if (terminalFilters != null && !terminalFilters.isEmpty()) {
            result = result.and(ClusterSpecifications.createForTerminalFilters(terminalFilters));
        }

        return result;
    }
}
