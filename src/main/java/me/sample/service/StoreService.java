package me.sample.service;

import me.sample.domain.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface StoreService {

    /**
     * Возвращает кол-во торговых точек, удовлетворяющих критерию поиска
     */
    Long countStores(Specification<Store> specification);

    /**
     * Возвращает список торговых точек
     */
    Page<Store> findStores(Pageable pageable);

    /**
     * Возвращает список торговых точек, удовлетворяющих критерию поиска
     */
    Page<Store> findStores(Specification<Store> specification, Pageable pageable);

    Set<String> findStoreCities();

    /**
     * Возвращает торговую точку с заданным id.
     */
    Optional<Store> findStore(UUID id);

    Store saveStore(Store data);

    Optional<Store> updateLocalStore(UUID id, Store data);

    Optional<Store> updateImportedStore(UUID id, Store data);

    long syncStoreCities();

    Optional<UUID> deleteStore(UUID id);
}
