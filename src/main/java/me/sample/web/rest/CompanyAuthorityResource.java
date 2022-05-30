package me.sample.web.rest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.CompanyAuthorityDto;
import me.sample.service.CompanyAuthorityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.web.rest.util.HeaderUtil;

import java.util.List;
import java.util.UUID;

@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
@Slf4j
@RequiredArgsConstructor
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/api/company-authority")
public class CompanyAuthorityResource {
    CompanyAuthorityService companyAuthorityService;

    @PostMapping
    public ResponseEntity<Void> setAuthority(@RequestBody CompanyAuthorityDto authorityDto) {
        companyAuthorityService.set(authorityDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createAlert("samplegatewayApp.samplebackendCompany.authority.notification.success", ""))
                .build();
    }

    @GetMapping("/{id}")
    public List<CompanyAuthorityDto> getAuthorities(@PathVariable("id") UUID id) {
        return companyAuthorityService.getAll(id);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAuthority(@RequestBody CompanyAuthorityDto authorityDto){
        companyAuthorityService.delete(authorityDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createAlert("samplegatewayApp.samplebackendCompany.authority.notification.delete", ""))
                .build();
    }
}
