package me.sample.web.rest;

import io.swagger.annotations.Api;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.ApplicationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.config.SwaggerConfiguration;
import me.sample.mapper.ApplicationMapper;
import me.sample.domain.Application;
import me.sample.domain.NotFoundResourceException;
import me.sample.service.SecuredApplicationService;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.util.HeaderUtil;
import me.sample.web.rest.util.PaginationUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Api(tags = SwaggerConfiguration.APPLICATION)
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
@RequestMapping("/api/applications")
@RestController
public class ApplicationResource {
    private static final String ENTITY_NAME = "samplebackendApplication";

    SecuredApplicationService securedApplicationService;

    ApplicationMapper applicationMapper;

    @GetMapping
    public ResponseEntity<List<ApplicationDTO>> index(@RequestParam(required = false) String search, Pageable pageable) {
        log.debug("REST request to get a page of Applications");
        Page<Application> applications = securedApplicationService.findApplications(search, pageable);

        Page<ApplicationDTO> result = applications
                .map(applicationMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/applications"))
                .body(result.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get Application : {}", id);

        Application application = securedApplicationService.findApplication(id)
                .orElseThrow(() -> new NotFoundResourceException("Application", id));

        return ResponseEntity.ok(applicationMapper.toDto(application));
    }

    @PostMapping
    public ResponseEntity<ApplicationDTO> save(@RequestBody ApplicationDTO request) throws URISyntaxException {
        log.debug("REST request to save Application : {}", request);
        if (request.getId() != null) {
            throw new BadRequestAlertException("A new application cannot already have an ID", ENTITY_NAME, "idexists");
        }

        if (request.getCompany() == null || request.getCompany().getId() == null) {
            throw new BadRequestAlertException("No company id provided", ENTITY_NAME, "idnull");
        }

        Application application = securedApplicationService.saveApplication(applicationMapper.toEntity(request));

        return ResponseEntity.created(new URI("/api/applications/" + application.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, application.getId().toString()))
                .body(applicationMapper.toDto(application));
    }

    @PutMapping
    public ResponseEntity<ApplicationDTO> update(@RequestBody ApplicationDTO request) throws URISyntaxException {
        log.debug("REST request to update Application : {}", request);

        UUID id = request.getId();
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Application application = securedApplicationService.updateApplication(id, applicationMapper.toEntity(request))
                .orElseThrow(() -> new NotFoundResourceException("Application", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, id.toString()))
                .body(applicationMapper.toDto(application));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> delete(@PathVariable UUID id) {
        log.debug("REST request to delete Application : {}", id);

        UUID applicationId = securedApplicationService.deleteApplication(id)
                .orElseThrow(() -> new NotFoundResourceException("Application", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .body(applicationId);
    }
}
