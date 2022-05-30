package me.sample.service;

import me.sample.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface CompanyService {

    Page<Company> findCompanies(Pageable pageable);

    Page<Company> findCompanies(Specification<Company> specification, Pageable pageable);

    Optional<Company> findCompany(UUID id);

    Optional<Company> findCompany(Specification<Company> specification);

    Company saveCompany(Company data);

    Optional<Company> updateCompany(UUID id, Company data);

    Optional<UUID> deleteCompany(UUID id);
}
