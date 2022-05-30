package me.sample.service;

import me.sample.domain.Promo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface SecuredPromoService {

    Page<Promo> findPromos(Pageable pageable);

    Page<Promo> findPromos(Specification<Promo> specification, Pageable pageable);

    Optional<Promo> findPromo(UUID id);

    Promo savePromo(Promo promo);

    Optional<Promo> updateLocalPromo(UUID id, Promo promo);

    Optional<UUID> deletePromo(UUID id);
}
