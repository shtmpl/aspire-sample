package me.sample.service;

import me.sample.domain.Distribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface DistributionService {

    Page<Distribution> findDisseminations(Specification<Distribution> specification, Pageable pageable);

    Optional<Distribution> findDissemination(UUID id);

    Distribution saveDissemination(Distribution data);

    Distribution startDissemination(Distribution found);

    Distribution pauseDissemination(Distribution found);

    Distribution completeDissemination(Distribution found);

    Optional<Distribution> executeDissemination(UUID id);

    Optional<UUID> deleteDissemination(UUID id);
}
