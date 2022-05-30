package me.sample.web.rest;

import io.swagger.annotations.Api;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.RequestLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import me.sample.config.SwaggerConfiguration;
import me.sample.mapper.RequestLogMapper;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.RequestLog;
import me.sample.domain.RequestLogCriteria;
import me.sample.domain.RequestLogSpecifications;
import me.sample.domain.Specifications;
import me.sample.service.SecuredRequestLogService;
import me.sample.web.rest.util.PaginationUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Api(tags = SwaggerConfiguration.REQUEST_LOG)
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
@RequestMapping("/api/request-logs")
@RestController
public class RequestLogResource {

    SecuredRequestLogService securedRequestLogService;

    RequestLogMapper requestLogMapper;

    @GetMapping
    public ResponseEntity<List<RequestLogDTO>> index(RequestLogCriteria criteria, Pageable pageable) {
        log.debug("REST request to get a page of RequestLogs");

        Page<RequestLog> requestLogs = securedRequestLogService.findRequestLogs(makeSpecification(criteria), pageable);
        Page<RequestLogDTO> result = requestLogs.map(requestLogMapper::toDto);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(result, "/api/request-log");
        return ResponseEntity.ok()
                .headers(headers)
                .body(result.getContent());
    }

    private Specification<RequestLog> makeSpecification(RequestLogCriteria request) {
        Specification<RequestLog> result = Specifications.any();

        UUID terminalId = request.getTerminalId();
        if (terminalId != null) {
            result = result.and(RequestLogSpecifications.terminalIdEqualTo(terminalId));
        }

        LocalDateTime from = request.getFrom();
        if (from != null) {
            result = result.and(RequestLogSpecifications.createdAtAfter(from));
        }

        LocalDateTime to = request.getTo();
        if (to != null) {
            result = result.and(RequestLogSpecifications.createdAtBefore(to));
        }

        String req = request.getRequest();
        if (req != null && !req.trim().isEmpty()) {
            result = result.and(RequestLogSpecifications.requestEqualTo(req));
        }

        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestLogDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get RequestLog : {}", id);

        RequestLog requestLog = securedRequestLogService.findRequestLog(id)
                .orElseThrow(() -> new NotFoundResourceException("RequestLog", id));

        return ResponseEntity.ok(requestLogMapper.toDto(requestLog));
    }
}
