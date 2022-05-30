package me.sample.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.StoreDTO;
import me.sample.dto.StoreSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.config.SwaggerConfiguration;
import me.sample.mapper.StoreMapper;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Source;
import me.sample.domain.Specifications;
import me.sample.domain.Store;
import me.sample.domain.StoreSpecifications;
import me.sample.domain.StoreState;
import me.sample.service.SecuredStoreService;
import me.sample.service.StoreService;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.util.HeaderUtil;
import me.sample.web.rest.util.PaginationUtil;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Api(tags = SwaggerConfiguration.STORE)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
@RequestMapping("/api/stores")
@RestController
public class StoreResource {

    private static final String ENTITY_NAME = "samplebackendStore";

    SecuredStoreService securedStoreService;
    StoreService storeService;

    StoreMapper storeMapper;

    /**
     * Возвращает список данных о торговых точках
     */
    @GetMapping
    public ResponseEntity<List<StoreDTO>> index(@PageableDefault(sort = {"partner.name", "id"}, direction = Sort.Direction.ASC)
                                                        Pageable pageable) {
        log.debug("REST request to get a page of Stores");

        Page<Store> stores = securedStoreService.findStores(pageable);
        Page<StoreDTO> result = stores.map(storeMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/stores"))
                .body(result.getContent());
    }

    @GetMapping("/city")
    public ResponseEntity<List<String>> indexCities() {
        Set<String> storeCities = storeService.findStoreCities();
        // TODO Возможно стоит выдавать только города доступных по правам ТТ

        return ResponseEntity.ok(storeCities.stream()
                .sorted()
                .collect(Collectors.toList()));
    }

    /**
     * Возвращает список данных о торговых точках, удовлетворяющих критерию поиска
     */
    @ApiOperation(
            value = "Поиск торговых точек",
            notes = "Поиск осуществляется по части вхождения строки поиска (query) в наименование торговой точки, либо в наименование города, либо в наименование ассоциированного партнера")
    @PostMapping("/search")
    public ResponseEntity<List<StoreDTO>> search(@Valid @RequestBody StoreSearchDTO request,
                                                 @PageableDefault(sort = {"partner.name", "id"}, direction = Sort.Direction.ASC)
                                                         Pageable pageable) {
        log.debug("REST request to get a page of Stores by filter");

        Page<Store> stores = securedStoreService.findStores(makeSpecification(request), pageable);
        Page<StoreDTO> result = stores.map(storeMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/stores/search"))
                .body(result.getContent());
    }

    /**
     * Возвращает кол-во торговых точкек, удовлетворяющих критерию поиска
     */
    @PostMapping("/search/count")
    public ResponseEntity<Long> countSearched(@Valid @RequestBody StoreSearchDTO request) {
        Long count = securedStoreService.countStores(makeSpecification(request));

        return ResponseEntity.ok(count);
    }

    private Specification<Store> makeSpecification(StoreSearchDTO request) {
        Specification<Store> result = Specifications.any();

        String query = request.getQuery();
        if (query != null && !query.trim().isEmpty()) {
            result = result.and(StoreSpecifications.nameLike(query)
                    .or(StoreSpecifications.cityLike(query))
                    .or(StoreSpecifications.partnerNameLike(query)));
        }

        List<UUID> partnerIds = request.getPartnerIds();
        if (partnerIds != null && !partnerIds.isEmpty()) {
            result = result.and(StoreSpecifications.partnerIdIn(partnerIds));
        }

        List<Source> sources = request.getSources();
        if (sources != null && !sources.isEmpty()) {
            result = result.and(StoreSpecifications.sourceIn(sources));
        }

        List<StoreState> states = request.getStates();
        if (states != null && !states.isEmpty()) {
            result = result.and(StoreSpecifications.stateIn(states));
        }

        return result;
    }

    /**
     * Возвращает данные о торговой точке с заданным id
     */
    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get Store : {}", id);

        Store store = securedStoreService.findStore(id)
                .orElseThrow(() -> new NotFoundResourceException("Store", id));

        return ResponseEntity.ok(storeMapper.toDto(store));
    }

    /**
     * Сохраняет данные о торговой точке
     */
    @PostMapping
    public ResponseEntity<StoreDTO> save(@Valid @RequestBody StoreDTO request) throws URISyntaxException {
        log.debug("REST request to save Store : {}", request);

        if (request.getId() != null) {
            throw new BadRequestAlertException("A new store cannot already have an ID", ENTITY_NAME, "idexists");
        }

        if (request.getPartner() == null || request.getPartner().getId() == null) {
            throw new BadRequestAlertException("No partner id provided", ENTITY_NAME, "idnull");
        }

        request.setSource(Source.LOCAL);
        Store store = securedStoreService.saveStore(storeMapper.toEntity(request));
        StoreDTO result = storeMapper.toDto(store);

        return ResponseEntity.created(new URI("/api/stores/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * Обновляет данные о торговой точке
     */
    @PutMapping
    public ResponseEntity<StoreDTO> update(@Valid @RequestBody StoreDTO request) throws URISyntaxException {
        log.debug("REST request to update Store : {}", request);

        UUID id = request.getId();
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Store store = securedStoreService.updateStore(id, storeMapper.toEntity(request))
                .orElseThrow(() -> new NotFoundResourceException("Store", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, id.toString()))
                .body(storeMapper.toDto(store));
    }

    /**
     * Удаляет данные о торговой точке
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> delete(@PathVariable UUID id) {
        log.debug("REST request to delete Store : {}", id);

        UUID storeId = securedStoreService.deleteStore(id)
                .orElseThrow(() -> new NotFoundResourceException("Store", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .body(storeId);
    }
}
