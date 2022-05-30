package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CompanyAuthorityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Application;
import me.sample.domain.ApplicationSpecifications;
import me.sample.service.ApplicationService;
import me.sample.service.SecuredApplicationService;
import me.sample.service.SecurityService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
@Service
public class SecuredApplicationServiceImpl implements SecuredApplicationService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    ApplicationService applicationService;

    @Transactional(readOnly = true)
    @Override
    public Page<Application> findApplications(String search, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findApplications(), User.id: {}", userId);

        if (search == null || search.trim().isEmpty()) {
            return applicationService.findApplications(ApplicationSpecifications.companyAuthorityUserIdEqualTo(userId), pageable);
        }

        return applicationService.findApplications(ApplicationSpecifications.companyAuthorityUserIdEqualTo(userId)
                .and(ApplicationSpecifications.nameLike(search).or(ApplicationSpecifications.apiKeyLike(search))), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Application> findApplication(UUID id) {
        log.debug(".findApplication(id: {})", id);

        Application found = applicationService.findApplication(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getCompany().getId());

        return Optional.of(found);
    }

    @Override
    public Application saveApplication(Application data) {
        log.debug(".saveApplication()");

        companyAuthorityService.checkWrite(data.getCompany().getId());

        return applicationService.saveApplication(data);
    }

    @Override
    public Optional<Application> updateApplication(UUID id, Application data) {
        log.debug(".updateApplication(id: {})", id);

        Application found = applicationService.findApplication(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(data.getCompany().getId());
        companyAuthorityService.checkWrite(found.getCompany().getId());

        Application result = applicationService.updateApplication(found, data);

        return Optional.of(result);
    }

    @Override
    public Optional<UUID> deleteApplication(UUID id) {
        log.debug(".deleteApplication(id: {})", id);

        Application found = applicationService.findApplication(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(found.getCompany().getId());

        return applicationService.deleteApplication(id);
    }
}
