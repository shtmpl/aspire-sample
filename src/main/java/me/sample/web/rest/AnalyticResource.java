package me.sample.web.rest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.enumeration.TimePeriod;
import me.sample.domain.AnalyticReport;
import me.sample.service.SecuredTerminalService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.domain.PosFrequency;
import me.sample.domain.PropFilter;
import me.sample.domain.TerminalSpecifications;
import me.sample.service.AnalyticService;
import me.sample.web.rest.util.ResponseUtil;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
@Slf4j
@RestController
@RequestMapping("/api/analytic")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AnalyticResource {
    AnalyticService analyticService;
    SecuredTerminalService securedTerminalService;

    @GetMapping("/pos-frequency")
    public List<PosFrequency> getPosFrequency(@RequestParam("timePeriod") TimePeriod timePeriod,
                                              @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
                                              @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to) {
        return analyticService.getPosFrequency(timePeriod, from, to);
    }

    @GetMapping("/vendor")
    public ResponseEntity<AnalyticReport> getVendorReport(@RequestParam TimePeriod timePeriod) {
        log.debug("REST request to get Vendor report : {}", timePeriod);
        Optional<AnalyticReport> report = analyticService.getVendorPercentages(timePeriod);
        return ResponseUtil.wrapOrNotFound(report);
    }

    @GetMapping("/age")
    public ResponseEntity<AnalyticReport> getAgeReport(@RequestParam TimePeriod timePeriod) {
        log.debug("REST request to get Age report : {}", timePeriod);
        Optional<AnalyticReport> report = analyticService.getAgePercentages(timePeriod);
        return ResponseUtil.wrapOrNotFound(report);
    }

    @GetMapping("/city")
    public ResponseEntity<AnalyticReport> getCityReport(@RequestParam TimePeriod timePeriod) {
        log.debug("REST request to get City report : {}", timePeriod);
        Optional<AnalyticReport> report = analyticService.getCityPercentages(timePeriod);
        return ResponseUtil.wrapOrNotFound(report);
    }

    @PostMapping("/auditory")
    public Long countAuditory(@Valid @RequestBody List<PropFilter> request) {
        return securedTerminalService.countTerminals(
                Specification.where(Specification.not(TerminalSpecifications.pushIdIsNull()))
                        .and(TerminalSpecifications.createForFilters(request)));
    }
}
