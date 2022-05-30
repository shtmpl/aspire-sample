package me.sample.web.rest;

import io.swagger.annotations.Api;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.GeoPositionInfoDTO;
import me.sample.dto.GeopositionSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import me.sample.config.SwaggerConfiguration;
import me.sample.domain.NotFoundResourceException;
import me.sample.mapper.GeoPositionInfoMapper;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.GeopositionSpecifications;
import me.sample.domain.Specifications;
import me.sample.service.SecuredGeoPositionInfoService;
import me.sample.web.rest.util.PaginationUtil;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Api(tags = SwaggerConfiguration.GEOPOSITION)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
@RequestMapping("/api/geopositions")
@RestController
public class GeopositionResource {

    SecuredGeoPositionInfoService securedGeoPositionInfoService;

    GeoPositionInfoMapper geoPositionInfoMapper;

    @GetMapping
    public ResponseEntity<List<GeoPositionInfoDTO>> index(Pageable pageable) {
        Page<GeoPositionInfo> geopositions = securedGeoPositionInfoService.findGeopositions(pageable);
        Page<GeoPositionInfoDTO> result = geopositions.map(geoPositionInfoMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/geopositions"))
                .body(result.getContent());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countIndexed() {
        Long count = securedGeoPositionInfoService.countGeopositions();

        return ResponseEntity.ok(count);
    }

    @PostMapping("/search")
    public ResponseEntity<List<GeoPositionInfoDTO>> search(@Valid @RequestBody GeopositionSearchDTO request,
                                                           Pageable pageable) {
        Specification<GeoPositionInfo> specification = makeSpecification(request);
        Page<GeoPositionInfo> geopositions = securedGeoPositionInfoService.findGeopositions(specification, pageable);
        Page<GeoPositionInfoDTO> result = geopositions.map(geoPositionInfoMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/geopositions/search"))
                .body(result.getContent());
    }

    private Specification<GeoPositionInfo> makeSpecification(GeopositionSearchDTO request) {
        Specification<GeoPositionInfo> result = Specifications.any();

        List<UUID> terminalIds = request.getTerminalIds();
        if (terminalIds != null && !terminalIds.isEmpty()) {
            result = result.and(GeopositionSpecifications.terminalIdIn(terminalIds));
        }

        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeoPositionInfoDTO> show(@PathVariable UUID id) {
        GeoPositionInfo geoPositionInfo = securedGeoPositionInfoService.findGeoposition(id)
                .orElseThrow(() -> new NotFoundResourceException("GeoPositionInfo", id));

        return ResponseEntity.ok(geoPositionInfoMapper.toDto(geoPositionInfo));
    }
}
