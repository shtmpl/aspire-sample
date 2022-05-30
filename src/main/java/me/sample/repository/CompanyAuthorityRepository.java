package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Permission;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface CompanyAuthorityRepository extends JpaRepository<CompanyAuthority, Long> {

    List<CompanyAuthority> findAllByUserId(Long userId);

    void deleteByUserIdAndCompanyId(Long userId, UUID companyId);

    Stream<CompanyAuthority> findAllByCompanyId(UUID companyId);

    boolean existsByUserIdAndCompanyIdAndPermissionIn(Long userId, UUID companyId, List<Permission> permissions);

    boolean existsByCompanyId(UUID companyId);
}
