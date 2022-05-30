package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Store;
import me.sample.domain.StoreSpecifications;
import me.sample.domain.StoreWithClusterCounts;
import me.sample.domain.StoreWithClusters;
import me.sample.domain.Terminal;
import me.sample.domain.geo.ClusterSpecifications;
import me.sample.domain.geo.Cluster;
import me.sample.repository.TerminalRepository;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.GeoAnalysisService;
import me.sample.service.SecuredGeoAnalysisService;
import me.sample.service.SecurityService;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class SecuredGeoAnalysisServiceImpl implements SecuredGeoAnalysisService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    GeoAnalysisService geoAnalysisService;

    TerminalRepository terminalRepository;

    @Override
    public Long countClusters() {
        Long userId = securityService.getUserId();
        log.debug(".countClusters(), User.id: {}", userId);

        return geoAnalysisService.countClusters(
                ClusterSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Cluster> findClusters(Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findClusters(), User.id: {}", userId);

        return geoAnalysisService.findClusters(
                ClusterSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Cluster> findClusters(Specification<Cluster> specification, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findClusters(), User.id: {}", userId);

        return geoAnalysisService.findClusters(
                ClusterSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId).and(specification),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Set<Cluster> findClustersByTerminalId(UUID terminalId) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new NotFoundResourceException("Terminal", terminalId));

        companyAuthorityService.checkRead(terminal.getApplication().getCompany().getId());

        return geoAnalysisService.findClustersByTerminalId(terminalId);
    }

    @Transactional(readOnly = true)
    @Override
    public StoreWithClusterCounts countStoresWithClusters(Long radius,
                                                          Specification<Store> storeSpecification,
                                                          Specification<Cluster> clusterSpecification) {
        Long userId = securityService.getUserId();
        log.debug(".countStoresWithClusters(), User.id: {}", userId);

        return geoAnalysisService.countStoresWithClusters(
                radius,
                Specification.where(storeSpecification)
                        .and(StoreSpecifications.partnerCompanyAuthorityUserIdEqualTo(userId)),
                Specification.where(clusterSpecification)
                        .and(ClusterSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId)));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StoreWithClusters> findStoresWithClusters(Long radius,
                                                          Specification<Store> storeSpecification,
                                                          Specification<Cluster> clusterSpecification,
                                                          Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findStoresWithClusters(), User.id: {}", userId);

        return geoAnalysisService.findStoresWithClusters(
                radius,
                Specification.where(storeSpecification)
                        .and(StoreSpecifications.partnerCompanyAuthorityUserIdEqualTo(userId)),
                Specification.where(clusterSpecification)
                        .and(ClusterSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId)),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StoreWithClusters> findStoreWithClusters(UUID storeId, Long radius,
                                                             Specification<Store> storeSpecification,
                                                             Specification<Cluster> clusterSpecification) {
        Long userId = securityService.getUserId();
        log.debug(".findStoreWithClusters(Store.id: {}), User.id: {}", storeId, userId);

        return geoAnalysisService.findStoreWithClusters(
                storeId, radius,
                Specification.where(storeSpecification)
                        .and(StoreSpecifications.partnerCompanyAuthorityUserIdEqualTo(userId)),
                Specification.where(clusterSpecification)
                        .and(ClusterSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId)));
    }
}
