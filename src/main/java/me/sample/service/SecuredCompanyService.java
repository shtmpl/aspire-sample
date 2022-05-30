package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import me.sample.domain.Company;

import java.util.Optional;
import java.util.UUID;

public interface SecuredCompanyService {

    Page<Company> findCompanies(Pageable pageable);

    Optional<Company> findCompany(UUID id);

    Company saveCompany(Company data);

    Optional<Company> updateCompany(UUID id, Company data);

    Optional<UUID> deleteCompany(UUID id);
}
