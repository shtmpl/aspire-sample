package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.gateway.dadata.DadataGateway;
import me.sample.domain.BadResourceException;
import me.sample.domain.Cities;
import me.sample.domain.Source;
import me.sample.domain.Store;
import me.sample.domain.StoreSpecifications;
import me.sample.domain.StoreState;
import me.sample.repository.StoreRepository;
import me.sample.service.StoreService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@CacheConfig(cacheNames = StoreServiceImpl.STORE_CACHE)
@Transactional
@Service
public class StoreServiceImpl implements StoreService {

    public static final String STORE_CACHE = "store";

    private static final int BATCH_SIZE_STORE_CITY_UPDATE = 1000;


    StoreRepository storeRepository;

    DadataGateway dadataGateway;

    @Transactional(readOnly = true)
    @Override
    public Long countStores(Specification<Store> specification) {
        return storeRepository.count(specification);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Store> findStores(Pageable pageable) {
        return storeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Store> findStores(Specification<Store> specification, Pageable pageable) {
        return storeRepository.findAll(specification, pageable);
    }

    @Cacheable(cacheNames = "store.city", cacheManager = "referenceCacheManager")
    @Transactional(readOnly = true)
    @Override
    public Set<String> findStoreCities() {
        log.debug(".findStoreCities()");

        return storeRepository.findDistinctCities().stream()
                .map(Cities::formatCityName)
                .filter((String city) -> !city.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Store> findStore(UUID id) {
        return storeRepository.findById(id);
    }

    @Override
    public Store saveStore(Store data) {
        UUID id = data.getId();
        if (id != null && storeRepository.existsById(id)) {
            throw new BadResourceException(String.format("Store already exists for id: %s", id));
        }

        return storeRepository.save(data);
    }

    @Override
    public Optional<Store> updateLocalStore(UUID id, Store data) {
        Store found = storeRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Source foundSource = found.getSource();
        if (foundSource != Source.LOCAL) {
            throw new UnsupportedOperationException(String.format(
                    "Недопустимо редактирование созданной по интеграции торговой точки: %s. Источник: %s",
                    id,
                    foundSource));
        }

        return updateStore(found, data);
    }

    @Override
    public Optional<Store> updateImportedStore(UUID id, Store data) {
        Store found = storeRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        return updateStore(found, data);
    }

    private Optional<Store> updateStore(Store found, Store store) {
        found.setUpdatedDate(LocalDateTime.now());

        StoreState state = store.getState();
        if (state != null && state != found.getState()) {
            found.setState(state);
        }

        String name = store.getName();
        if (name != null && !name.equals(found.getName())) {
            found.setName(name);
        }

        String fiasCode = store.getFiasCode();
        if (fiasCode != null && !fiasCode.equals(found.getFiasCode())) {
            found.setFiasCode(fiasCode);
        }

        String kladrCode = store.getKladrCode();
        if (kladrCode != null && !kladrCode.equals(found.getKladrCode())) {
            found.setKladrCode(kladrCode);
        }

        String city = store.getCity();
        if (city != null && !city.equals(found.getCity())) {
            found.setCity(city);
        }

        String address = store.getAddress();
        if (address != null && !address.equals(found.getAddress())) {
            found.setAddress(address);
        }

        Double lat = store.getLat();
        if (lat != null && !lat.equals(found.getLat())) {
            found.setLat(lat);
        }

        Double lon = store.getLon();
        if (lon != null && !lon.equals(found.getLon())) {
            found.setLon(lon);
        }

        return Optional.of(storeRepository.save(found));
    }

    @Override
    public long syncStoreCities() {
        log.debug(".syncStoreCities()");

        long result = 0;
        Page<Store> page;
        while ((page = storeRepository.findAll(
                StoreSpecifications.cityIsBlank()
                        .and(Specification.not(StoreSpecifications.latIsNull()))
                        .and(Specification.not(StoreSpecifications.lonIsNull())),
                PageRequest.of(0, BATCH_SIZE_STORE_CITY_UPDATE))).hasContent()) {
            log.info("Sync'ing city for {} terminals...", page.getNumberOfElements());
            result += page.getNumberOfElements();

            storeRepository.saveAll(page.map(this::alterStoreCity));
        }

        return result;
    }

    private Store alterStoreCity(Store found) {
        String city = dadataGateway.findCityByCoordinates(found.getLat(), found.getLon())
                .orElse(null);
        if (city != null && !city.equals(found.getCity())) {
            log.debug(".city: {} -> {}", found.getCity(), city);
            found.setCity(city);
        }

        return found;
    }

    @Override
    public Optional<UUID> deleteStore(UUID id) {
        Store found = storeRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Source source = found.getSource();
        if (source != Source.LOCAL) {
            throw new UnsupportedOperationException(String.format(
                    "Недопустимо удаление созданной по интеграции торговой точки: %s. Источник: %s",
                    id,
                    source));
        }

        storeRepository.delete(found);

        return Optional.of(id);
    }
}
