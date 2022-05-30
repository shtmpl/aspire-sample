package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.RequestLog;
import me.sample.domain.RequestLogSpecifications;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.RequestLogService;
import me.sample.service.SecuredRequestLogService;
import me.sample.service.SecurityService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SecuredRequestLogServiceImpl implements SecuredRequestLogService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    RequestLogService requestLogService;

    @Override
    public Page<RequestLog> findRequestLogs(Specification<RequestLog> specification, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findRequestLogs(), User.id: {}", userId);

        return requestLogService.findRequestLogs(RequestLogSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId).and(specification), pageable);
    }

    @Override
    public Optional<RequestLog> findRequestLog(UUID id) {
        log.debug(".findRequestLog(id: {})", id);

        RequestLog found = requestLogService.findRequestLog(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getTerminal().getApplication().getCompany().getId());

        return Optional.of(found);
    }
}
