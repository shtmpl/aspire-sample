package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.BadResourceException;
import me.sample.domain.Company;
import me.sample.repository.CompanyRepository;
import me.sample.service.CompanyService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@CacheConfig(cacheNames = CompanyServiceImpl.COMPANY_CACHE)
@Transactional
@Service
public class CompanyServiceImpl implements CompanyService {

    public static final String COMPANY_CACHE = "company";

    CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<Company> findCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Company> findCompanies(Specification<Company> specification, Pageable pageable) {
        return companyRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Company> findCompany(UUID id) {
        return companyRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Company> findCompany(Specification<Company> specification) {
        return companyRepository.findOne(specification);
    }

    @Override
    public Company saveCompany(Company data) {
        UUID id = data.getId();
        if (id != null && companyRepository.existsById(id)) {
            throw new BadResourceException(String.format("Company already exists for id: %s", id));
        }

        return companyRepository.save(data);
    }

    @Override
    public Optional<Company> updateCompany(UUID id, Company data) {
        return companyRepository.findById(id)
                .map((Company found) -> updateCompany(found, data));
    }

    private Company updateCompany(Company found, Company data) {
        String name = data.getName();
        if (name != null && !name.equals(found.getName())) {
            found.setName(name);
        }

        String description = data.getDescription();
        if (description != null && !description.equals(found.getDescription())) {
            found.setDescription(description);
        }

        return companyRepository.save(found);
    }

    @Override
    public Optional<UUID> deleteCompany(UUID id) {
        Company found = companyRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyRepository.deleteById(id);

        return Optional.of(id);
    }
}
