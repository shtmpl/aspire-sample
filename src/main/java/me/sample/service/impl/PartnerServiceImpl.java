package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.PartnerService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.BadResourceException;
import me.sample.domain.Partner;
import me.sample.domain.PartnerState;
import me.sample.domain.Source;
import me.sample.repository.PartnerRepository;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@CacheConfig(cacheNames = PartnerServiceImpl.PARTNER_CACHE)
@Transactional
@Service
public class PartnerServiceImpl implements PartnerService {

    public static final String PARTNER_CACHE = "partner";

    PartnerRepository partnerRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<Partner> findPartners(Pageable pageable) {
        return partnerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Partner> findPartners(Specification<Partner> specification, Pageable pageable) {
        return partnerRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Partner> findPartner(UUID id) {
        return partnerRepository.findById(id);
    }

    @CacheEvict(allEntries = true)
    @Override
    public Partner savePartner(Partner data) {
        UUID id = data.getId();
        if (id != null && partnerRepository.existsById(id)) {
            throw new BadResourceException(String.format("Partner already exists for id: %s", id));
        }

        return partnerRepository.save(data);
    }

    @Override
    public Optional<Partner> updateLocalPartner(UUID id, Partner data) {
        Partner found = partnerRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Source foundSource = found.getSource();
        if (foundSource != Source.LOCAL) {
            throw new UnsupportedOperationException(String.format(
                    "Недопустимо редактирование созданного по интеграции партнера: %s. Источник: %s",
                    id,
                    foundSource));
        }

        return updatePartner(found, data);
    }

    @Override
    public Optional<Partner> updateImportedPartner(UUID id, Partner data) {
        Partner found = partnerRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        return updatePartner(found, data);
    }

    private Optional<Partner> updatePartner(Partner found, Partner partner) {
        found.setUpdatedDate(LocalDateTime.now());

        PartnerState state = partner.getState();
        if (state != null && state != found.getState()) {
            found.setState(state);
        }

        String name = partner.getName();
        if (name != null && !name.equals(found.getName())) {
            found.setName(name);
        }

        String descriptionFull = partner.getDescriptionFull();
        if (descriptionFull != null && !descriptionFull.equals(found.getDescriptionFull())) {
            found.setDescriptionFull(descriptionFull);
        }

        String descriptionShort = partner.getDescriptionShort();
        if (descriptionShort != null && !descriptionShort.equals(found.getDescriptionShort())) {
            found.setDescriptionShort(descriptionShort);
        }

        String siteUrl = partner.getSiteUrl();
        if (siteUrl != null && !siteUrl.equals(found.getSiteUrl())) {
            found.setSiteUrl(siteUrl);
        }

        Boolean sitePayment = partner.getSitePayment();
        if (sitePayment != null && !sitePayment.equals(found.getSitePayment())) {
            found.setSitePayment(sitePayment);
        }

        Boolean deliveryRussia = partner.getDeliveryRussia();
        if (deliveryRussia != null && !deliveryRussia.equals(found.getDeliveryRussia())) {
            found.setDeliveryRussia(deliveryRussia);
        }

        Boolean hasOnlineStore = partner.getHasOnlineStore();
        if (hasOnlineStore != null && !hasOnlineStore.equals(found.getHasOnlineStore())) {
            found.setHasOnlineStore(hasOnlineStore);
        }

        Boolean hasOfflineStore = partner.getHasOfflineStore();
        if (hasOfflineStore != null && !hasOfflineStore.equals(found.getHasOfflineStore())) {
            found.setHasOfflineStore(hasOfflineStore);
        }

        String pinIconUrl = partner.getPinIconUrl();
        if (pinIconUrl != null && !pinIconUrl.equals(found.getPinIconUrl())) {
            found.setPinIconUrl(pinIconUrl);
        }

        String iconUrl = partner.getIconUrl();
        if (iconUrl != null && !iconUrl.equals(found.getIconUrl())) {
            found.setIconUrl(iconUrl);
        }

        return Optional.of(partnerRepository.save(found));
    }

    @CacheEvict(allEntries = true)
    @Override
    public Optional<UUID> deletePartner(UUID id) {
        Partner found = partnerRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Source source = found.getSource();
        if (source != Source.LOCAL) {
            throw new UnsupportedOperationException(String.format(
                    "Недопустимо удаление созданного по интеграции партнера: %s. Источник: %s",
                    id,
                    source));
        }

        partnerRepository.deleteById(id);

        return Optional.of(id);
    }
}
