package me.sample.service;

import me.sample.dto.CompanyAuthorityDto;
import me.sample.domain.Company;

import java.util.List;
import java.util.UUID;

public interface CompanyAuthorityService {
    void checkRead(UUID companyId);

    void checkWrite(UUID companyId);

    void setOriginal(Company company);

    void set(CompanyAuthorityDto authority);

    List<CompanyAuthorityDto> getAll(UUID companyId);

    void delete(CompanyAuthorityDto authorityDto);
}
