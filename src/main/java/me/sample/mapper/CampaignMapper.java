package me.sample.mapper;

import me.sample.dto.CampaignDTO;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignState;
import me.sample.repository.CampaignClientRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Mapper(
        uses = {
                DistributionMapper.class,
                ScheduledGeoposDisseminationMapper.class
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CampaignMapper {

    @Autowired
    protected CampaignClientRepository campaignClientRepository;

    @Mapping(target = "status", source = "state")
    @Mapping(target = "clientIdCount", ignore = true)
    public abstract CampaignDTO toDto(Campaign campaign);

    @AfterMapping
    public void setClientIdCount(@MappingTarget CampaignDTO response, Campaign campaign) {
        if (Boolean.TRUE.equals(campaign.getClientBased())) {
            response.setClientIdCount(campaignClientRepository.countAllByCampaignId(campaign.getId()));
        }
    }

    @Mapping(target = "state", source = "request.status")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "company", qualifiedBy = OnlyId.class)
    @Mapping(target = "distribution", qualifiedBy = OnlyId.class)
    @Mapping(target = "scheduledGeoposDissemination", qualifiedBy = OnlyId.class)
    @Mapping(target = "sent", ignore = true)
    @Mapping(target = "opened", ignore = true)
    @Mapping(target = "delivered", ignore = true)
    @Mapping(target = "fail", ignore = true)
    public abstract Campaign toEntity(CampaignDTO request);

    @AfterMapping
    public void setStatus(@MappingTarget Campaign campaign, CampaignDTO request) {
        if (Objects.isNull(request.getStatus())) {
            campaign.setState(CampaignState.CREATED);
        }
    }
}
