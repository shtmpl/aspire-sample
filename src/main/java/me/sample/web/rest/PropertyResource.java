package me.sample.web.rest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.PropertyDTO;
import me.sample.service.PropertyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.mapper.PropertyMapper;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Property;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.util.HeaderUtil;
import me.sample.web.rest.util.PaginationUtil;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
@RequestMapping("/api/properties")
@RestController
public class PropertyResource {

    static final String ENTITY_NAME = "samplebackendProperty";

    PropertyService propertyService;

    PropertyMapper propertyMapper;

    @GetMapping
    public ResponseEntity<List<PropertyDTO>> index(Pageable pageable) {
        log.debug("REST request to get a page of Properties");

        Page<Property> properties = propertyService.findProperties(pageable);
        Page<PropertyDTO> result = properties.map(propertyMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/properties"))
                .body(result.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get Property : {}", id);

        Property property = propertyService.findProperty(id)
                .orElseThrow(() -> new NotFoundResourceException("Property", id));

        return ResponseEntity.ok(propertyMapper.toDto(property));
    }

    @PostMapping
    public ResponseEntity<PropertyDTO> save(@Valid @RequestBody PropertyDTO request) throws URISyntaxException {
        log.debug("REST request to save Property : {}", request);

        if (request.getId() != null) {
            throw new BadRequestAlertException("A new property cannot already have an ID", ENTITY_NAME, "idexists");
        }

        Property property = propertyService.saveProperty(propertyMapper.toEntity(request));
        PropertyDTO result = propertyMapper.toDto(property);

        return ResponseEntity.created(new URI("/api/properties/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping
    public ResponseEntity<PropertyDTO> update(@Valid @RequestBody PropertyDTO request) throws URISyntaxException {
        log.debug("REST request to update Property : {}", request);

        UUID id = request.getId();
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Property property = propertyService.updateProperty(id, propertyMapper.toEntity(request))
                .orElseThrow(() -> new NotFoundResourceException("Property", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, id.toString()))
                .body(propertyMapper.toDto(property));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> delete(@PathVariable UUID id) {
        log.debug("REST request to delete Property : {}", id);

        UUID propertyId = propertyService.deleteProperty(id)
                .orElseThrow(() -> new NotFoundResourceException("Property", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .body(propertyId);
    }
}
