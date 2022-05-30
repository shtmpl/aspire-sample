package me.sample.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import me.sample.domain.Company;
import me.sample.domain.CompanySpecifications;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID>, JpaSpecificationExecutor<Company> {

    default Company findFirstForUser(Long userId) {
        return findAll(CompanySpecifications.authorityUserIdEqualTo(userId), PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "updatedDate")))
                .stream()
                .findFirst()
                .orElse(null);
    }
}
