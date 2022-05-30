package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Company;
import me.sample.domain.CompanySpecifications;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.CompanyService;
import me.sample.service.SecuredCompanyService;
import me.sample.service.SecurityService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class SecuredCompanyServiceImpl implements SecuredCompanyService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    CompanyService companyService;

    @Transactional(readOnly = true)
    @Override
    public Page<Company> findCompanies(Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findCompanies(), User.id: {}", userId);

        return companyService.findCompanies(CompanySpecifications.authorityUserIdEqualTo(userId), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Company> findCompany(UUID id) {
        log.debug(".findCompany(id: {})", id);

        companyAuthorityService.checkRead(id);

        return companyService.findCompany(id);
    }

    @Override
    public Company saveCompany(Company data) {
        log.debug(".saveCompany()");

        // FIXME: No security check?

        Company result = companyService.saveCompany(data);
        companyAuthorityService.setOriginal(result);

        return result;
    }

    @Override
    public Optional<Company> updateCompany(UUID id, Company data) {
        log.debug(".updateCompany(id: {})", id);

        Company found = companyService.findCompany(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(id);
        companyAuthorityService.checkWrite(data.getId());

        return companyService.updateCompany(id, data);
    }

    @Override
    public Optional<UUID> deleteCompany(UUID id) {
        log.debug(".deleteCompany(id: {})", id);

        companyAuthorityService.checkWrite(id);

        return companyService.deleteCompany(id);
    }
}
