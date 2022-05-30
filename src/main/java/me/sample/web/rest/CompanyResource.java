package me.sample.web.rest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.CompanyDTO;
import me.sample.service.SecuredCompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.mapper.CompanyMapper;
import me.sample.domain.Company;
import me.sample.domain.NotFoundResourceException;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.util.HeaderUtil;
import me.sample.web.rest.util.PaginationUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
@RequestMapping("/api/companies")
@RestController
public class CompanyResource {
    private static final String ENTITY_NAME = "samplebackendCompany";

    SecuredCompanyService securedCompanyService;

    CompanyMapper companyMapper;

    @GetMapping
    public ResponseEntity<List<CompanyDTO>> index(Pageable pageable) {
        log.debug("REST request to get a page of Companies");

        Page<Company> companies = securedCompanyService.findCompanies(pageable);
        Page<CompanyDTO> result = companies.map(companyMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/companies"))
                .body(result.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get Company : {}", id);

        Company company = securedCompanyService.findCompany(id)
                .orElseThrow(() -> new NotFoundResourceException("Company", id));

        return ResponseEntity.ok(companyMapper.toDto(company));
    }

    @PostMapping
    public ResponseEntity<CompanyDTO> save(@RequestBody CompanyDTO request) throws URISyntaxException {
        log.debug("REST request to save Company : {}", request);

        if (request.getId() != null) {
            throw new BadRequestAlertException("A new company cannot already have an ID", ENTITY_NAME, "idexists");
        }

        Company company = securedCompanyService.saveCompany(companyMapper.toEntity(request));
        CompanyDTO result = companyMapper.toDto(company);

        return ResponseEntity.created(new URI("/api/companys/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping
    public ResponseEntity<CompanyDTO> update(@RequestBody CompanyDTO request) throws URISyntaxException {
        log.debug("REST request to update Company : {}", request);

        UUID id = request.getId();
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Company company = securedCompanyService.updateCompany(id, companyMapper.toEntity(request))
                .orElseThrow(() -> new NotFoundResourceException("Company", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, id.toString()))
                .body(companyMapper.toDto(company));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> delete(@PathVariable UUID id) {
        log.debug("REST request to delete Company : {}", id);

        UUID companyId = securedCompanyService.deleteCompany(id)
                .orElseThrow(() -> new NotFoundResourceException("Company", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .body(companyId);
    }
}
