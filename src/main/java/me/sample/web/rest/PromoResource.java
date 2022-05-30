package me.sample.web.rest;

import io.swagger.annotations.Api;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.PromoDTO;
import me.sample.dto.PromoSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import me.sample.config.SwaggerConfiguration;
import me.sample.domain.NotFoundResourceException;
import me.sample.mapper.PromoMapper;
import me.sample.domain.Promo;
import me.sample.domain.PromoSpecifications;
import me.sample.domain.PromoState;
import me.sample.domain.Source;
import me.sample.domain.Specifications;
import me.sample.service.SecuredPromoService;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.util.HeaderUtil;
import me.sample.web.rest.util.PaginationUtil;
import me.sample.web.rest.util.ResponseUtil;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Api(tags = SwaggerConfiguration.PROMO)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
@RequestMapping("/api/promos")
@RestController
public class PromoResource {

    private static final String ENTITY_NAME = "samplebackendPromo";

    SecuredPromoService securedPromoService;
    PromoMapper promoMapper;

    @GetMapping
    public ResponseEntity<List<PromoDTO>> index(Pageable pageable) {
        log.debug("REST request to get a page of Promos");

        Page<Promo> promos = securedPromoService.findPromos(pageable);
        Page<PromoDTO> result = promos.map(promoMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/promos"))
                .body(result.getContent());
    }

    @PostMapping("/search")
    public ResponseEntity<List<PromoDTO>> search(@Valid @RequestBody PromoSearchDTO request,
                                                 Pageable pageable) {
        log.debug("REST request to get a page of Promos");

        Page<Promo> promos = securedPromoService.findPromos(makeSpecification(request), pageable);
        Page<PromoDTO> result = promos.map(promoMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/promos/search"))
                .body(result.getContent());
    }

    private Specification<Promo> makeSpecification(PromoSearchDTO request) {
        Specification<Promo> result = Specifications.any();

        String query = request.getQuery();
        if (query != null && !query.trim().isEmpty()) {
            result = result.and(PromoSpecifications.nameLike(query).or(PromoSpecifications.descriptionLike(query)));
        }

        List<Source> sources = request.getSources();
        if (sources != null && !sources.isEmpty()) {
            result = result.and(PromoSpecifications.sourceIn(sources));
        }

        List<PromoState> states = request.getStates();
        if (states != null && !states.isEmpty()) {
            result = result.and(PromoSpecifications.stateIn(states));
        }

        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get Promo: {}", id);

        Optional<PromoDTO> result = securedPromoService.findPromo(id)
                .map(promoMapper::toDto);

        return ResponseUtil.wrapOrNotFound(result);
    }

    @PostMapping
    public ResponseEntity<PromoDTO> save(@Valid @RequestBody PromoDTO dto) throws URISyntaxException {
        log.debug("REST request to save Promo: {}", dto);

        if (dto.getId() != null) {
            throw new BadRequestAlertException("A new promo cannot already have an ID", ENTITY_NAME, "idexists");
        }

        dto.setSource(Source.LOCAL);
        PromoDTO result = promoMapper.toDto(securedPromoService.savePromo(promoMapper.toEntity(dto)));

        return ResponseEntity.created(new URI("/api/promos/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping
    public ResponseEntity<PromoDTO> update(@Valid @RequestBody PromoDTO dto) throws URISyntaxException {
        log.debug("REST request to update Promo: {}", dto);

        UUID id = dto.getId();
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        PromoDTO result = securedPromoService.updateLocalPromo(id, promoMapper.toEntity(dto))
                .map(promoMapper::toDto)
                .orElseThrow(() -> new NotFoundResourceException("Promo", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, id.toString()))
                .body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> delete(@PathVariable UUID id) {
        log.debug("REST request to delete Promo: {}", id);

        UUID result = securedPromoService.deletePromo(id)
                .orElseThrow(() -> new NotFoundResourceException("Promo", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .body(result);
    }
}
