package me.sample.service.impl;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CompanyAuthorityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.dto.CompanyAuthorityDto;
import me.sample.domain.NotFoundResourceException;
import me.sample.mapper.CompanyAuthorityMapper;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Permission;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.service.SecurityService;
import me.sample.web.rest.errors.AuthorityException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CompanyAuthorityServiceImpl implements CompanyAuthorityService {

    SecurityService securityService;
    CompanyRepository companyRepository;
    CompanyAuthorityRepository companyAuthorityRepository;
    CompanyAuthorityMapper companyAuthorityMapper;

    @Override
    public void checkRead(@NonNull UUID companyId) {
        boolean hasAuthority = hasAuthority(companyId, Lists.newArrayList(Permission.READ, Permission.READ_WRITE));

        if (!hasAuthority) throw new AuthorityException();
    }

    @Override
    public void checkWrite(@NonNull UUID companyId) {
        boolean hasAuthority = hasAuthority(companyId, Lists.newArrayList(Permission.READ_WRITE));

        if (!hasAuthority) throw new AuthorityException();
    }


    private boolean hasAuthority(UUID companyId, List<Permission> permissions) {
        Long userId = securityService.getUserId();
        log.debug("Check company authority: companyId = {}, userId = {}, permissions = {}", companyId, userId, permissions);
        return companyAuthorityRepository.existsByUserIdAndCompanyIdAndPermissionIn(userId, companyId, permissions);
    }

    @Override
    @Transactional
    public void setOriginal(@NonNull Company company) {
        Optional.ofNullable(company.getId())
                .orElseThrow(() -> new IllegalArgumentException("Set original authority operation has to be invoked after the entity has been persisted"));

        if (companyAuthorityRepository.existsByCompanyId(company.getId())) {
            throw new AuthorityException("Company already has an authority");
        }

        CompanyAuthority companyAuthority = CompanyAuthority.builder()
                .userId(securityService.getUserId())
                .company(company)
                .permission(Permission.READ_WRITE)
                .build();

        companyAuthorityRepository.save(companyAuthority);
    }

    @Override
    @Transactional
    public void set(@NonNull CompanyAuthorityDto authority) {
        validate(authority);

        UUID companyId = authority.getCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundResourceException("Company", String.valueOf(companyId)));

        CompanyAuthority companyAuthority = CompanyAuthority.builder()
                .userId(authority.getUserId())
                .company(company)
                .permission(authority.getPermission())
                .build();

        companyAuthorityRepository.save(companyAuthority);
    }

    @Override
    public List<CompanyAuthorityDto> getAll(UUID companyId) {
        checkRead(companyId);
        return companyAuthorityRepository.findAllByCompanyId(companyId)
                .map(companyAuthorityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void delete(CompanyAuthorityDto authority) {
        validate(authority);

        companyAuthorityRepository.deleteByUserIdAndCompanyId(authority.getUserId(), authority.getCompanyId());
    }

    private void validate(CompanyAuthorityDto authority) {
        if (Objects.equals(authority.getUserId(), securityService.getUserId()))
            throw new AuthorityException("User can't change self authority");

        Optional.ofNullable(authority.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Set additional authority operation can be made only if the entity is persisted"));
        checkWrite(authority.getCompanyId());
    }
}
