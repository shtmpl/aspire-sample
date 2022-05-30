package me.sample.web.rest;

import io.swagger.annotations.Api;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.NotificationTemplateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.config.SwaggerConfiguration;
import me.sample.mapper.NotificationTemplateMapper;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.NotificationTemplate;
import me.sample.service.SecuredNotificationTemplateService;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.util.HeaderUtil;
import me.sample.web.rest.util.PaginationUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing NotificationTemplate.
 */
@Api(tags = SwaggerConfiguration.NOTIFICATION_TEMPLATE)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
@RequestMapping("/api/notification-templates")
@RestController
public class NotificationTemplateResource {

    private static final String ENTITY_NAME = "samplebackendNotificationTemplate";

    SecuredNotificationTemplateService securedNotificationTemplateService;

    NotificationTemplateMapper notificationTemplateMapper;

    /**
     * GET  /notification-templates : get all the notificationTemplates.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of notificationTemplates in body
     */
    @GetMapping
    public ResponseEntity<List<NotificationTemplateDTO>> index(@RequestParam(required = false) String search, Pageable pageable) {
        log.debug("REST request to search a page of NotificationTemplates");

        Page<NotificationTemplate> notificationTemplates = securedNotificationTemplateService.findNotificationTemplates(search, pageable);
        Page<NotificationTemplateDTO> result = notificationTemplates.map(notificationTemplateMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/notification-templates"))
                .body(result.getContent());
    }


    /**
     * GET  /notification-templates/:id : get the "id" notificationTemplate.
     *
     * @param id the id of the notificationTemplate to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the notificationTemplate, or with status 404 (Not Found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplateDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get NotificationTemplate : {}", id);

        NotificationTemplate notificationTemplate = securedNotificationTemplateService.findNotificationTemplate(id)
                .orElseThrow(() -> new NotFoundResourceException("NotificationTemplate", id));

        return ResponseEntity.ok(notificationTemplateMapper.toDto(notificationTemplate));
    }

    /**
     * POST  /notification-templates : Create a new notificationTemplate.
     *
     * @param request the notificationTemplate to create
     * @return the ResponseEntity with status 201 (Created) and with body the new notificationTemplate, or with status 400 (Bad Request) if the notificationTemplate has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping
    public ResponseEntity<NotificationTemplateDTO> save(@RequestBody NotificationTemplateDTO request) throws URISyntaxException {
        log.debug("REST request to save NotificationTemplate : {}", request);

        UUID id = request.getId();
        if (id != null) {
            throw new BadRequestAlertException("A new notificationTemplate cannot already have an ID", ENTITY_NAME, "idexists");
        }

        if (request.getCompany() == null || request.getCompany().getId() == null) {
            throw new BadRequestAlertException("No company id provided", ENTITY_NAME, "idnull");
        }

        NotificationTemplate notificationTemplate = securedNotificationTemplateService.saveNotificationTemplate(notificationTemplateMapper.toEntity(request));
        NotificationTemplateDTO result = notificationTemplateMapper.toDto(notificationTemplate);

        return ResponseEntity.created(new URI("/api/notification-templates/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /notification-templates : Updates an existing notificationTemplate.
     *
     * @param request the notificationTemplate to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated notificationTemplate,
     * or with status 400 (Bad Request) if the notificationTemplate is not valid,
     * or with status 500 (Internal Server Error) if the notificationTemplate couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping
    public ResponseEntity<NotificationTemplateDTO> update(@RequestBody NotificationTemplateDTO request) throws URISyntaxException {
        log.debug("REST request to update NotificationTemplate : {}", request);

        UUID id = request.getId();
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        NotificationTemplate notificationTemplate = securedNotificationTemplateService.updateNotificationTemplate(id, notificationTemplateMapper.toEntity(request))
                .orElseThrow(() -> new NotFoundResourceException("NotificationTemplate", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, id.toString()))
                .body(notificationTemplateMapper.toDto(notificationTemplate));
    }

    /**
     * DELETE  /notification-templates/:id : delete the "id" notificationTemplate.
     *
     * @param id the id of the notificationTemplate to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> delete(@PathVariable UUID id) {
        log.debug("REST request to delete NotificationTemplate : {}", id);

        UUID notificationTemplateId = securedNotificationTemplateService.deleteNotificationTemplate(id)
                .orElseThrow(() -> new NotFoundResourceException("NotificationTemplate", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .body(notificationTemplateId);
    }
}
