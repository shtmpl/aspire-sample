package me.sample.service;

import me.sample.domain.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface SecuredStoreService {

    /**
     * Возвращает кол-во торговых точек, удовлетворяющих критерию поиска, доступных текущему пользователю
     */
    Long countStores(Specification<Store> specification);

    /**
     * Возвращает список торговых точек, доступных текущему пользователю.
     */
    Page<Store> findStores(Pageable pageable);

    /**
     * Возвращает список торговых точек, удовлетворяющих критерию поиска и доступных текущему пользователю.
     */
    Page<Store> findStores(Specification<Store> specification, Pageable pageable);

    /**
     * Возвращает торговую точку с заданным id.
     */
    Optional<Store> findStore(UUID id);

    Store saveStore(Store data);

    Optional<Store> updateStore(UUID id, Store data);

    /**
     * Удаляет торговую точку с заданным id.
     */
    Optional<UUID> deleteStore(UUID id);
}
