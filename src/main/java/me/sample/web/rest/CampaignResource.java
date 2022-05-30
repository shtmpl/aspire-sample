package me.sample.web.rest;

import io.swagger.annotations.Api;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.CampaignDTO;
import me.sample.dto.CampaignSearchDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.config.SwaggerConfiguration;
import me.sample.mapper.CampaignMapper;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignSpecifications;
import me.sample.domain.CampaignState;
import me.sample.domain.Specifications;
import me.sample.service.SecuredCampaignService;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.util.HeaderUtil;
import me.sample.web.rest.util.PaginationUtil;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

/**
 * Ресурсы '*Resource' - это контроллеры, предназначенные только для внутреннего
 * использования. Т.е. например для обслуживания запросов админки
 */
@Api(tags = SwaggerConfiguration.CAMPAIGN)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
@RequestMapping("/api/campaigns")
@RestController
public class CampaignResource {

    private static final String ENTITY_NAME = "samplebackendCampaign";

    private static final long DEFAULT_CAMPAIGN_RADIUS = 500L;
    private static final long DEFAULT_NOTIFICATION_LIMIT_PER_TERMINAL = 1L;

    SecuredCampaignService securedCampaignService;

    CampaignMapper campaignMapper;

    @GetMapping
    public ResponseEntity<List<CampaignDTO>> index(@SortDefault(sort = "id", direction = Sort.Direction.ASC)
                                                           Pageable pageable) {
        log.debug("REST request to get a page of Campaigns");

        Page<Campaign> campaigns = securedCampaignService.findCampaigns(pageable);
        Page<CampaignDTO> result = campaigns.map(campaignMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/campaigns"))
                .body(result.getContent());
    }

    @PostMapping("/search")
    public ResponseEntity<List<CampaignDTO>> search(@Valid @RequestBody CampaignSearchDTO request,
                                                    @SortDefault(sort = "id", direction = Sort.Direction.ASC)
                                                            Pageable pageable) {
        Specification<Campaign> specification = makeSpecification(request);
        Page<Campaign> campaigns = securedCampaignService.findCampaigns(specification, pageable);
        Page<CampaignDTO> result = campaigns.map(campaignMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/campaigns/search"))
                .body(result.getContent());
    }

    private Specification<Campaign> makeSpecification(CampaignSearchDTO request) {
        Specification<Campaign> result = Specifications.any();

        List<CampaignState> states = request.getStates();
        if (states != null && !states.isEmpty()) {
            result = result.and(CampaignSpecifications.stateIn(states));
        }

        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get Campaign : {}", id);

        Campaign campaign = securedCampaignService.findCampaign(id)
                .orElseThrow(() -> new NotFoundResourceException("Campaign", id));

        return ResponseEntity.ok(campaignMapper.toDto(campaign));
    }

    @PostMapping
    public ResponseEntity<CampaignDTO> save(@Valid @RequestBody CampaignDTO request) throws URISyntaxException {
        log.debug("REST request to save Campaign : {}", request);
        if (request.getId() != null) {
            throw new BadRequestAlertException("A new campaign cannot already have an ID", ENTITY_NAME, "idexists");
        }

        if (request.getCompany() == null || request.getCompany().getId() == null) {
            throw new BadRequestAlertException("No company id provided", ENTITY_NAME, "idnull");
        }

        if (request.getRadius() == null) {
            request.setRadius(DEFAULT_CAMPAIGN_RADIUS);
        }

        request.setNotificationLimitPerTerminal(DEFAULT_NOTIFICATION_LIMIT_PER_TERMINAL);

        if (request.getDistribution() == null && request.getScheduledGeoposDissemination() == null) {
            throw new BadRequestAlertException(
                    "Neither distribution nor scheduledGeoposDissemination is provided. There should be exactly one of those",
                    ENTITY_NAME,
                    "disseminationnull");
        } else if (request.getDistribution() != null && request.getScheduledGeoposDissemination() != null) {
            throw new BadRequestAlertException(
                    "Both distribution and scheduledGeoposDissemination is provided. There should be exactly one of those",
                    ENTITY_NAME,
                    "disseminationboth");
        }


        String clientIdDataContentType = request.getClientIdDataContentType();
        String clientIdDataBase64 = request.getClientIdDataBase64();
        if (StringUtils.isBlank(clientIdDataContentType) && StringUtils.isNotBlank(clientIdDataBase64)) {
            throw new BadRequestAlertException(
                    "Invalid clientIdDataContentType provided",
                    ENTITY_NAME,
                    "clientiddatacontenttypeinvalid");
        } else if (StringUtils.isNotBlank(clientIdDataContentType) && StringUtils.isBlank(clientIdDataBase64)) {
            throw new BadRequestAlertException(
                    "No clientIdDataBase64 provided",
                    ENTITY_NAME,
                    "clientiddatanull");
        }

        Campaign campaign = securedCampaignService.saveCampaign(
                campaignMapper.toEntity(request),
                clientIdDataContentType,
                clientIdDataBase64);

        CampaignDTO result = campaignMapper.toDto(campaign);

        return ResponseEntity.created(new URI("/api/campaigns/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @GetMapping("/start/{id}")
    public void start(@PathVariable UUID id) {
        log.debug("REST request to start Campaign : {}", id);

        securedCampaignService.startCampaign(id)
                .orElseThrow(() -> new NotFoundResourceException("Campaign", id));
    }

    @GetMapping("/pause/{id}")
    public void pause(@PathVariable UUID id) {
        log.debug("REST request to pause Campaign : {}", id);

        securedCampaignService.pauseCampaign(id)
                .orElseThrow(() -> new NotFoundResourceException("Campaign", id));
    }

    @GetMapping("/complete/{id}")
    public void complete(@PathVariable UUID id) {
        log.debug("REST request to complete Campaign : {}", id);

        securedCampaignService.completeCampaign(id)
                .orElseThrow(() -> new NotFoundResourceException("Campaign", id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> delete(@PathVariable UUID id) {
        log.debug("REST request to delete Campaign : {}", id);

        UUID campaignId = securedCampaignService.deleteCampaign(id)
                .orElseThrow(() -> new NotFoundResourceException("Campaign", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .body(campaignId);
    }
}
