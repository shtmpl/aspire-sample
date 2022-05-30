package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.PartnerService;
import me.sample.service.SecuredPartnerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Partner;
import me.sample.domain.PartnerSpecifications;
import me.sample.service.SecurityService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
@Service
public class SecuredPartnerServiceImpl implements SecuredPartnerService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    PartnerService partnerService;

    @Transactional(readOnly = true)
    @Override
    public Page<Partner> findPartners(Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findPartners(), User.id: {}", userId);

        return partnerService.findPartners(PartnerSpecifications.companyAuthorityUserIdEqualTo(userId), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Partner> findPartners(Specification<Partner> specification, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findPartners(), User.id: {}", userId);

        return partnerService.findPartners(PartnerSpecifications.companyAuthorityUserIdEqualTo(userId).and(specification), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Partner> findPartner(UUID id) {
        log.debug(".findPartner(id: {})", id);

        Partner found = partnerService.findPartner(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getCompany().getId());

        return Optional.of(found);
    }

    @Override
    public Partner savePartner(Partner data) {
        log.debug(".savePartner()");

        companyAuthorityService.checkWrite(data.getCompany().getId());

        return partnerService.savePartner(data);
    }

    @Override
    public Optional<Partner> updatePartner(UUID id, Partner data) {
        log.debug(".updatePartner(id: {})", id);

        Partner found = partnerService.findPartner(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(data.getCompany().getId());
        companyAuthorityService.checkWrite(found.getCompany().getId());

        return partnerService.updateLocalPartner(id, data);
    }

    @Override
    public Optional<UUID> deletePartner(UUID id) {
        log.debug(".deletePartner(id: {})", id);

        Partner found = partnerService.findPartner(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(found.getCompany().getId());

        return partnerService.deletePartner(id);
    }
}
