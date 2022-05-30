package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.PromoService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.BadResourceException;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Partner;
import me.sample.domain.Promo;
import me.sample.domain.PromoState;
import me.sample.domain.Source;
import me.sample.domain.Store;
import me.sample.repository.PartnerRepository;
import me.sample.repository.PromoRepository;
import me.sample.repository.StoreRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@CacheConfig(cacheNames = PromoServiceImpl.CACHE_NAME)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Service
public class PromoServiceImpl implements PromoService {

    public static final String CACHE_NAME = "promo";

    PromoRepository promoRepository;

    PartnerRepository partnerRepository;
    StoreRepository storeRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<Promo> findPromos(Pageable pageable) {
        return promoRepository.findAll(pageable);
    }

    @Cacheable
    @Transactional(readOnly = true)
    @Override
    public Page<Promo> findPromos(Specification<Promo> specification, Pageable pageable) {
        return promoRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Promo> findPromo(UUID id) {
        return promoRepository.findById(id);
    }

    @CacheEvict(allEntries = true)
    @Override
    public Promo savePromo(Promo data) {
        UUID id = data.getId();
        if (id != null && promoRepository.existsById(id)) {
            throw new BadResourceException(String.format("Promo already exists for id: %s", id));
        }

        Promo result = promoRepository.save(data);

        Set<Partner> partners = data.getPartners().stream()
                .map(Partner::getId)
                .map((UUID partnerId) -> partnerRepository.findById(partnerId)
                        .orElseThrow(() -> new NotFoundResourceException("Partner", partnerId)))
                .peek((Partner partner) -> partner.getPromos().add(result))
                .collect(Collectors.toSet());
        partnerRepository.saveAll(partners);

        Set<Store> stores = data.getStores().stream()
                .map(Store::getId)
                .map((UUID storeId) -> storeRepository.findById(storeId)
                        .orElseThrow(() -> new NotFoundResourceException("Store", storeId)))
                .peek((Store store) -> store.getPromos().add(result))
                .collect(Collectors.toSet());
        storeRepository.saveAll(stores);

        return result;
    }

    @Override
    public Optional<Promo> updateLocalPromo(UUID id, Promo data) {
        Promo found = promoRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Source foundSource = found.getSource();
        if (foundSource != Source.LOCAL) {
            throw new UnsupportedOperationException(String.format(
                    "Недопустимо редактирование созданной по интеграции акции: %s. Источник: %s", id, foundSource));
        }

        return Optional.of(updatePromo(found, data));
    }

    @Override
    public Optional<Promo> updateImportedPromo(UUID id, Promo data) {
        Promo found = promoRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        return Optional.of(updatePromo(found, data));
    }

    private Promo updatePromo(Promo found, Promo promo) {
        found.setUpdatedDate(LocalDateTime.now());

        PromoState state = promo.getState();
        if (state != null && state != found.getState()) {
            found.setState(state);
        }

        Boolean hidden = promo.getHidden();
        if (hidden != null && !hidden.equals(found.getHidden())) {
            found.setHidden(hidden);
        }

        LocalDateTime activatedAt = promo.getActivatedAt();
        if (activatedAt != null && !activatedAt.equals(found.getActivatedAt())) {
            found.setActivatedAt(activatedAt);
        }

        LocalDateTime deactivatedAt = promo.getDeactivatedAt();
        if (deactivatedAt != null && !deactivatedAt.equals(found.getDeactivatedAt())) {
            found.setDeactivatedAt(deactivatedAt);
        }

        String name = promo.getName();
        if (name != null && !name.equals(found.getName())) {
            found.setName(name);
        }

        String description = promo.getDescription();
        if (description != null && !description.equals(found.getDescription())) {
            found.setDescription(description);
        }

        String imageUrl = promo.getImageUrl();
        if (imageUrl != null && !imageUrl.equals(found.getImageUrl())) {
            found.setImageUrl(imageUrl);
        }

        Promo result = promoRepository.save(found);


        Set<UUID> partnerIds = promo.getPartners().stream()
                .map(Partner::getId)
                .collect(Collectors.toSet());

        Set<Partner> associatedPartners = result.getPartners();
        Set<UUID> associatedPartnerIds = associatedPartners.stream()
                .map(Partner::getId)
                .collect(Collectors.toSet());

        Set<Partner> removedPartners = associatedPartners.stream()
                .filter((Partner partner) -> !partnerIds.contains(partner.getId()))
                .peek((Partner partner) -> partner.getPromos().remove(result))
                .collect(Collectors.toSet());
        partnerRepository.saveAll(removedPartners);

        Set<Partner> addedPartners = partnerIds.stream()
                .filter((UUID partnerId) -> !associatedPartnerIds.contains(partnerId))
                .map((UUID partnerId) -> partnerRepository.findById(partnerId)
                        .orElseThrow(() -> new NotFoundResourceException("Partner", partnerId)))
                .peek((Partner partner) -> partner.getPromos().add(result))
                .collect(Collectors.toSet());
        partnerRepository.saveAll(addedPartners);


        Set<UUID> storeIds = promo.getStores().stream()
                .map(Store::getId)
                .collect(Collectors.toSet());

        Set<Store> associatedStores = result.getStores();
        Set<UUID> associatedStoreIds = associatedStores.stream()
                .map(Store::getId)
                .collect(Collectors.toSet());

        Set<Store> removedStores = associatedStores.stream()
                .filter((Store store) -> !storeIds.contains(store.getId()))
                .peek((Store store) -> store.getPromos().remove(result))
                .collect(Collectors.toSet());
        storeRepository.saveAll(removedStores);

        Set<Store> addedStores = storeIds.stream()
                .filter((UUID storeId) -> !associatedStoreIds.contains(storeId))
                .map((UUID storeId) -> storeRepository.findById(storeId)
                        .orElseThrow(() -> new NotFoundResourceException("Store", storeId)))
                .peek((Store store) -> store.getPromos().add(result))
                .collect(Collectors.toSet());
        storeRepository.saveAll(addedStores);


        return result;
    }

    @CacheEvict(allEntries = true)
    @Override
    public Optional<UUID> deletePromo(UUID id) {
        Promo found = promoRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Source foundSource = found.getSource();
        if (foundSource != Source.LOCAL) {
            throw new UnsupportedOperationException(String.format(
                    "Недопустимо удаление созданной по интеграции акции: %s. Источник: %s", id, foundSource));
        }

        promoRepository.delete(found);

        return Optional.of(id);
    }
}
