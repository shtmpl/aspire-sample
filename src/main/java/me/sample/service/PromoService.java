package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.Promo;

import java.util.Optional;
import java.util.UUID;

public interface PromoService {

    Page<Promo> findPromos(Pageable pageable);

    Page<Promo> findPromos(Specification<Promo> specification, Pageable pageable);

    Optional<Promo> findPromo(UUID id);

    Promo savePromo(Promo data);

    Optional<Promo> updateLocalPromo(UUID id, Promo data);

    Optional<Promo> updateImportedPromo(UUID id, Promo data);

    Optional<UUID> deletePromo(UUID id);
}
