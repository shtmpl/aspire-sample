package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import me.sample.domain.Partner;
import me.sample.domain.Promo;
import me.sample.domain.Store;

import java.util.Set;
import java.util.UUID;

public interface PromoRepository extends JpaRepository<Promo, UUID>, JpaSpecificationExecutor<Promo> {

    Set<Promo> findAllByPartnersInOrStoresIn(Set<Partner> partners, Set<Store> stores);
}
