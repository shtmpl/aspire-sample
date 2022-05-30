package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import me.sample.domain.Distribution;

import java.util.UUID;

public interface DistributionRepository extends JpaRepository<Distribution, UUID>, JpaSpecificationExecutor<Distribution> {
}
