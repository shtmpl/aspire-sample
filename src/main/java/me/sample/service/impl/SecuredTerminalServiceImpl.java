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
import me.sample.domain.Terminal;
import me.sample.domain.TerminalSpecifications;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.SecuredTerminalService;
import me.sample.service.SecurityService;
import me.sample.service.TerminalService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class SecuredTerminalServiceImpl implements SecuredTerminalService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    TerminalService terminalService;

    @Override
    public long countTerminals(Specification<Terminal> specification) {
        Long userId = securityService.getUserId();
        log.debug(".countTerminals(), User.id: {}", userId);

        return terminalService.countTerminals(
                TerminalSpecifications.applicationCompanyAuthorityUserIdEqualTo(userId)
                        .and(specification));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Terminal> findTerminals(Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findTerminals(), User.id: {}", userId);

        return terminalService.findTerminals(
                TerminalSpecifications.applicationCompanyAuthorityUserIdEqualTo(userId),
                pageable);
    }

    @Override
    public Page<Terminal> findTerminals(Specification<Terminal> specification, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findTerminals(), User.id: {}", userId);

        return terminalService.findTerminals(
                TerminalSpecifications.applicationCompanyAuthorityUserIdEqualTo(userId).and(specification),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Terminal> findTerminal(UUID id) {
        log.debug(".findTerminal(id: {})", id);

        Terminal found = terminalService.findTerminal(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getApplication().getCompany().getId());

        return Optional.of(found);
    }
}
