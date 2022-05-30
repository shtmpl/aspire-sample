package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CampaignService;
import me.sample.service.CompanyAuthorityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignSpecifications;
import me.sample.service.SecuredCampaignService;
import me.sample.service.SecurityService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class SecuredCampaignServiceImpl implements SecuredCampaignService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    CampaignService campaignService;

    @Transactional(readOnly = true)
    @Override
    public Page<Campaign> findCampaigns(Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findCampaigns(), User.id: {}", userId);

        return campaignService.findCampaigns(CampaignSpecifications.companyAuthorityUserIdEqualTo(userId), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Campaign> findCampaigns(Specification<Campaign> specification, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findCampaigns(), User.id: {}", userId);

        return campaignService.findCampaigns(CampaignSpecifications.companyAuthorityUserIdEqualTo(userId).and(specification), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Campaign> findCampaign(UUID id) {
        log.debug(".findCampaign(id: {})", id);

        Campaign found = campaignService.findCampaign(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getCompany().getId());

        return Optional.of(found);
    }

    @Override
    public Campaign saveCampaign(Campaign data, String clientIdDataContentType, String clientIdDataBase64) {
        log.debug(".saveCampaign()");

        companyAuthorityService.checkWrite(data.getCompany().getId());

        Campaign result = campaignService.saveCampaign(data);
        if (clientIdDataBase64 == null || clientIdDataBase64.trim().isEmpty()) {
            return result;
        }

        if (clientIdDataContentType == null || clientIdDataContentType.trim().isEmpty()) {
            throw new UnsupportedOperationException("No clientIdDataContentType provided");
        }

        campaignService.updateCampaignClientsFromData(result, clientIdDataContentType, clientIdDataBase64);

        return result;
    }

    @Override
    public Optional<Campaign> startCampaign(UUID id) {
        log.debug(".startCampaign(id: {})", id);

        Campaign found = campaignService.findCampaign(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(found.getCompany().getId());

        return Optional.of(campaignService.startCampaign(found));
    }

    @Override
    public Optional<Campaign> pauseCampaign(UUID id) {
        log.debug(".pauseCampaign(id: {})", id);

        Campaign found = campaignService.findCampaign(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(found.getCompany().getId());

        return Optional.of(campaignService.pauseCampaign(found));
    }

    @Override
    public Optional<Campaign> completeCampaign(UUID id) {
        log.debug(".completeCampaign(id: {})", id);

        Campaign found = campaignService.findCampaign(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(found.getCompany().getId());

        return Optional.of(campaignService.completeCampaign(found));
    }

    @Override
    public Optional<UUID> deleteCampaign(UUID id) {
        log.debug(".deleteCampaign(id: {})", id);

        Campaign found = campaignService.findCampaign(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(found.getCompany().getId());

        return campaignService.deleteCampaign(id);
    }
}
