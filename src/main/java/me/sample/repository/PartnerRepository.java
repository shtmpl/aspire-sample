package me.sample.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import me.sample.domain.Partner;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID>, JpaSpecificationExecutor<Partner> {

    @EntityGraph(attributePaths = {"promos"})
    Optional<Partner> findWithPromosById(UUID id);

    @EntityGraph(attributePaths = {"stores", "promos"})
    Optional<Partner> findWithStoresAndPromosById(UUID id);
}
