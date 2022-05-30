package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.GeoPositionInfoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.GeopositionSpecifications;
import me.sample.service.SecuredGeoPositionInfoService;
import me.sample.service.SecurityService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class SecuredGeoPositionInfoServiceImpl implements SecuredGeoPositionInfoService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    GeoPositionInfoService geoPositionInfoService;

    @Transactional(readOnly = true)
    @Override
    public Long countGeopositions() {
        Long userId = securityService.getUserId();
        log.debug(".findGeopositions(), User.id: {}", userId);

        return geoPositionInfoService.countGeopositions(
                GeopositionSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GeoPositionInfo> findGeopositions(Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findGeopositions(), User.id: {}", userId);

        return geoPositionInfoService.findGeopositions(
                GeopositionSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GeoPositionInfo> findGeopositions(Specification<GeoPositionInfo> specification, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findGeopositions(), User.id: {}", userId);

        return geoPositionInfoService.findGeopositions(GeopositionSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId).and(specification), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<GeoPositionInfo> findGeoposition(UUID id) {
        log.debug(".findGeoposition(id: {})", id);

        GeoPositionInfo found = geoPositionInfoService.findGeoposition(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getTerminal().getApplication().getCompany().getId());

        return Optional.of(found);
    }
}
