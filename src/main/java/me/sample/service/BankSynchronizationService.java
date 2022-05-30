package me.sample.service;

import me.sample.domain.Category;
import me.sample.domain.Partner;
import me.sample.domain.Promo;
import me.sample.domain.Store;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис синхронизации данных с API банка
 */
public interface BankSynchronizationService {

    String COMPANY_ID_BANK = "28d95f8c-317c-4a7b-8ea8-c4d94353270a";

    /**
     * Импортирует все доступные сущности
     */
    void syncResources();

    /**
     * Импортирует сущности {@link Category}
     */
    void syncCategories();

    /**
     * Импортирует сущности {@link Partner}.
     * Импортирует ассоциированные сущности {@link Store} и {@link Promo}
     */
    void syncPartnersAndAssociations();

    Optional<Partner> syncPartnerAndAssociations(UUID id);

    Partner savePartnerAndAssociations(Partner partner, Set<Store> stores, Set<Promo> promos);
}
