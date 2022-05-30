package me.sample.web.rest;

import io.swagger.annotations.Api;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.TerminalDTO;
import me.sample.dto.TerminalSearchDTO;
import me.sample.service.SecuredTerminalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.config.SwaggerConfiguration;
import me.sample.domain.NotFoundResourceException;
import me.sample.mapper.TerminalMapper;
import me.sample.domain.Specifications;
import me.sample.domain.Terminal;
import me.sample.domain.TerminalSpecifications;
import me.sample.web.rest.util.PaginationUtil;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Api(tags = SwaggerConfiguration.TERMINAL)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
@RequestMapping("/api/terminals")
@RestController
public class TerminalResource {

    static final String ENTITY_NAME = "samplebackendTerminal";

    SecuredTerminalService securedTerminalService;

    TerminalMapper terminalMapper;

    @GetMapping
    public ResponseEntity<List<TerminalDTO>> index(@SortDefault("id") Pageable pageable) {
        log.debug("REST request to get a page of Terminals");

        Page<Terminal> terminals = securedTerminalService.findTerminals(pageable);
        Page<TerminalDTO> result = terminals.map(terminalMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/terminals"))
                .body(result.getContent());
    }

    @PostMapping("/search")
    public ResponseEntity<List<TerminalDTO>> search(@Valid @RequestBody TerminalSearchDTO request,
                                                    @SortDefault("id") Pageable pageable) {
        Page<Terminal> terminals = securedTerminalService.findTerminals(makeSpecification(request), pageable);
        Page<TerminalDTO> result = terminals.map(terminalMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/terminals/search"))
                .body(result.getContent());
    }

    private Specification<Terminal> makeSpecification(TerminalSearchDTO request) {
        Specification<Terminal> result = Specifications.any();

        List<UUID> ids = request.getIds();
        if (ids != null && !ids.isEmpty()) {
            result = result.and(TerminalSpecifications.idIn(ids));
        }

        List<String> hardwareIds = request.getHardwareIds();
        if (hardwareIds != null && !hardwareIds.isEmpty()) {
            result = result.and(TerminalSpecifications.hardwareIdIn(hardwareIds));
        }

        List<String> vendors = request.getVendors();
        if (vendors != null && !vendors.isEmpty()) {
            result = result.and(TerminalSpecifications.vendorIn(vendors));
        }

        List<String> models = request.getModels();
        if (models != null && !models.isEmpty()) {
            result = result.and(TerminalSpecifications.modelIn(models));
        }

        List<String> osVersions = request.getOsVersions();
        if (osVersions != null && !osVersions.isEmpty()) {
            result = result.and(TerminalSpecifications.osVersionIn(osVersions));
        }

        List<String> clientIds = request.getClientIds();
        if (clientIds != null && !clientIds.isEmpty()) {
            result = result.and(TerminalSpecifications.propClientIdIn(clientIds));
        }

        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerminalDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get Terminal : {}", id);

        Terminal terminal = securedTerminalService.findTerminal(id)
                .orElseThrow(() -> new NotFoundResourceException("Terminal", id));

        return ResponseEntity.ok(terminalMapper.toDto(terminal));
    }

    // FIXME: Пока скрываем
//    @PatchMapping("/{id}")
//    public ResponseEntity<TerminalDTO> alter(@PathVariable UUID id,
//                                             @Valid @RequestBody TerminalDTO request) {
//        Terminal result = terminalService.updateTerminal(id, Terminal.builder()
//                .test(request.getTest())
//                .build())
//                .orElseThrow(() -> new ResourceNotFoundException("Terminal", id));
//
//        return ResponseEntity.ok(terminalMapper.toDto(result));
//    }
//
//    @PostMapping
//    public ResponseEntity<TerminalDTO> createTerminal(@RequestBody TerminalDTO terminal) throws URISyntaxException {
//        log.debug("REST request to save Terminal : {}", terminal);
//        if (terminal.getId() != null) {
//            throw new BadRequestAlertException("A new terminal cannot already have an ID", ENTITY_NAME, "idexists");
//        }
//        TerminalDTO result = terminalService.create(terminal);
//        return ResponseEntity.created(new URI("/api/terminals/" + result.getId()))
//                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
//                .body(result);
//    }
//
//    @PutMapping
//    public ResponseEntity<TerminalDTO> updateTerminal(@RequestBody TerminalDTO terminal) throws URISyntaxException {
//        log.debug("REST request to update Terminal : {}", terminal);
//        if (terminal.getId() == null) {
//            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
//        }
//        TerminalDTO result = terminalService.create(terminal);
//        return ResponseEntity.ok()
//                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, terminal.getId().toString()))
//                .body(result);
//    }
//
//    @PostMapping("/test")
//    public UUID sendTestPush(@RequestBody TestPushDTO testPushDTO) {
//        return terminalService.sendTestPush(testPushDTO.getTerminalId(), testPushDTO.getGeoposDisseminationId());
//    }
//
//    @PostMapping("/import/check")
//    public TerminalImportReportDTO checkImport(@RequestBody List<TerminalDTO> imports) {
//        return terminalService.checkImport(imports);
//    }
//
//    @PostMapping("/import")
//    public void saveImport(@RequestBody List<TerminalDTO> imports) {
//        terminalService.saveImport(imports);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<UUID> deleteTerminal(@PathVariable UUID id) {
//        log.debug("REST request to delete Terminal : {}", id);
//        UUID result = terminalService.delete(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Terminal", id));
//
//        return ResponseEntity.ok()
//                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
//                .body(result);
//    }
}
