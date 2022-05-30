package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.Partner;

import java.util.Optional;
import java.util.UUID;

public interface PartnerService {

    /**
     * Ищет всех партнеров. Постраничный вывод.
     *
     * @param pageable информация о постраничном выводе
     * @return список партнеров
     */
    Page<Partner> findPartners(Pageable pageable);

    /**
     * Ищет всех партнеров, удовлетворяющих критериям поиска. Постраничный вывод.
     *
     * @param specification критерии поиска
     * @param pageable информация о постраничном выводе
     * @return список партнеров
     */
    Page<Partner> findPartners(Specification<Partner> specification, Pageable pageable);

    /**
     * Ищет партнера с заданным id
     *
     * @param id id партнера
     * @return партнер
     */
    Optional<Partner> findPartner(UUID id);

    /**
     * Сохраняет партнера
     *
     * @param data партнер
     * @return партнер
     */
    Partner savePartner(Partner data);

    /**
     * Обновляет локально созданного партнера с заданным id
     */
    Optional<Partner> updateLocalPartner(UUID id, Partner data);

    /**
     * Обновляет импортированного партнера с заданным id
     */
    Optional<Partner> updateImportedPartner(UUID id, Partner data);

    /**
     * Удаляет партнера с заданным id
     *
     * @param id id партнера
     */
    Optional<UUID> deletePartner(UUID id);
}
