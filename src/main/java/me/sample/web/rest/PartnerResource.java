package me.sample.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.PartnerDTO;
import me.sample.dto.PartnerSearchDTO;
import me.sample.service.SecuredPartnerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.config.SwaggerConfiguration;
import me.sample.mapper.PartnerMapper;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Partner;
import me.sample.domain.PartnerSpecifications;
import me.sample.domain.PartnerState;
import me.sample.domain.Source;
import me.sample.domain.Specifications;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.util.HeaderUtil;
import me.sample.web.rest.util.PaginationUtil;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для управления партнерами.
 * Эндпоинт для работы фронтенда
 */
@Api(tags = SwaggerConfiguration.PARTNER)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
@RequestMapping("/api/partners")
@RestController
public class PartnerResource {

    private static final String ENTITY_NAME = "samplebackendPartner";

    SecuredPartnerService securedPartnerService;

    PartnerMapper partnerMapper;

    /**
     * GET  /partners : возвращает список партнеров доступных текущему пользователю.
     *
     * @param pageable детали постраничного вывода
     * @return объект ResponseEntity со статусом 200 (OK) и список партнеров в теле ответа
     */
    @GetMapping
    public ResponseEntity<List<PartnerDTO>> index(@SortDefault(sort = "id", direction = Sort.Direction.ASC)
                                                          Pageable pageable) {
        log.debug("REST request to search a page of Partners");
        Page<Partner> partners = securedPartnerService.findPartners(pageable);
        Page<PartnerDTO> result = partners.map(partnerMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/partners"))
                .body(result.getContent());
    }

    /**
     * POST  /search : возвращает список партнеров, удовлетворяющих заданному поисковому запросу,
     * доступных текущему пользователю.
     *
     * @param request  критерии поиска партнеров
     * @param pageable детали постраничного вывода
     * @return объект ResponseEntity со статусом 200 (OK) и список партнеров в теле ответа
     */
    @ApiOperation(
            value = "Поиск партнеров",
            notes = "Поиск осуществляется по части вхождения строки поиска (query) в наименование партнера")
    @PostMapping("/search")
    public ResponseEntity<List<PartnerDTO>> search(@Valid @RequestBody PartnerSearchDTO request,
                                                   @SortDefault(sort = "id", direction = Sort.Direction.ASC)
                                                           Pageable pageable) {
        log.debug("REST request to search a page of Partners");

        Specification<Partner> specification = makeSpecification(request);
        Page<Partner> partners = securedPartnerService.findPartners(specification, pageable);
        Page<PartnerDTO> result = partners.map(partnerMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/partners/search"))
                .body(result.getContent());
    }

    private Specification<Partner> makeSpecification(PartnerSearchDTO request) {
        Specification<Partner> result = Specifications.any();

        List<UUID> ids = request.getIds();
        if (ids != null && !ids.isEmpty()) {
            result = result.and(PartnerSpecifications.idIn(ids));
        }

        List<Source> sources = request.getSources();
        if (sources != null && !sources.isEmpty()) {
            result = result.and(PartnerSpecifications.sourceIn(sources));
        }

        List<PartnerState> states = request.getStates();
        if (states != null && !states.isEmpty()) {
            result = result.and(PartnerSpecifications.stateIn(states));
        }

        String query = request.getQuery();
        if (query != null && !query.trim().isEmpty()) {
            result = result.and(PartnerSpecifications.nameLike(query));
        }

        return result;
    }

    /**
     * Возвращает партнера с заданным id
     *
     * @param id id партнера
     * @return ответ со статусом 200 (OK) и данными партнера в теле ответа,
     * ответ со статусом 404 (Not Found), если партнер не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<PartnerDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get Partner : {}", id);

        Partner partner = securedPartnerService.findPartner(id)
                .orElseThrow(() -> new NotFoundResourceException("Partner", id));

        return ResponseEntity.ok(partnerMapper.toDto(partner));
    }

    /**
     * POST  /partners : Создает нового партнера
     *
     * @param request партнер
     * @return ответ со статусом 201 (Created) и данными партнера в теле ответа,
     * ответ со статусом 400 (Bad Request) если у партнера уже есть id
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping
    public ResponseEntity<PartnerDTO> save(@Valid @RequestBody PartnerDTO request) throws URISyntaxException {
        log.debug("REST request to save Partner : {}", request);

        if (request.getId() != null) {
            throw new BadRequestAlertException("A new partner cannot already have an ID", ENTITY_NAME, "idexists");
        }

        if (request.getCompany() == null || request.getCompany().getId() == null) {
            throw new BadRequestAlertException("No company id provided", ENTITY_NAME, "idnull");
        }

        request.setSource(Source.LOCAL);
        Partner partner = securedPartnerService.savePartner(partnerMapper.toEntity(request));
        PartnerDTO result = partnerMapper.toDto(partner);

        return ResponseEntity.created(new URI("/api/partners/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /partners : Обновляет существующего партнера
     *
     * @param request партнер
     * @return ответ со статусом 200 (OK) и данными партнера в теле ответа;
     * ответ со статусом 400 (Bad Request), если партнер невалидный;
     * ответ со статусом 500 (Internal Server Error), если партнер не может быть обновлен.
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping
    public ResponseEntity<PartnerDTO> update(@Valid @RequestBody PartnerDTO request) throws URISyntaxException {
        log.debug("REST request to update Partner : {}", request);

        UUID id = request.getId();
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Partner partner = securedPartnerService.updatePartner(id, partnerMapper.toEntity(request))
                .orElseThrow(() -> new NotFoundResourceException("Partner", String.valueOf(id)));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, String.valueOf(id)))
                .body(partnerMapper.toDto(partner));
    }

    /**
     * DELETE  /partners/:id : Удаляет партнера
     *
     * @param id id партнера
     * @return ответ со статусом 200 (OK) и id удаленного партнера в теле ответа
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> delete(@PathVariable UUID id) {
        log.debug("REST request to delete Partner : {}", id);

        UUID partnerId = securedPartnerService.deletePartner(id)
                .orElseThrow(() -> new NotFoundResourceException("Partner", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .body(partnerId);
    }
}
