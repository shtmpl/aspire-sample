package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.PartnerService;
import me.sample.service.PromoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Company;
import me.sample.domain.Partner;
import me.sample.domain.Promo;
import me.sample.domain.PromoSpecifications;
import me.sample.domain.Store;
import me.sample.service.SecuredPromoService;
import me.sample.service.SecurityService;
import me.sample.service.StoreService;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Service
public class SecuredPromoServiceImpl implements SecuredPromoService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    PromoService promoService;

    PartnerService partnerService;
    StoreService storeService;

    @Transactional(readOnly = true)
    @Override
    public Page<Promo> findPromos(Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findPromos(), User.id: {}", userId);

        return promoService.findPromos(PromoSpecifications.partnerCompanyAuthorityUserIdEqualTo(userId)
                .or(PromoSpecifications.storePartnerCompanyAuthorityUserIdEqualTo(userId)), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Promo> findPromos(Specification<Promo> specification, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findPromos(), User.id: {}", userId);

        return promoService.findPromos(
                specification.and(PromoSpecifications.partnerCompanyAuthorityUserIdEqualTo(userId)
                        .or(PromoSpecifications.storePartnerCompanyAuthorityUserIdEqualTo(userId))),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Promo> findPromo(UUID id) {
        log.debug(".findPromo(id: {})", id);

        Promo found = promoService.findPromo(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        requireReadAccess(found);

        return promoService.findPromo(id);
    }

    @Override
    public Promo savePromo(Promo promo) {
        log.debug(".savePromo()");

        requireWriteAccessForEachPartner(promo.getPartners().stream()
                .map(Partner::getId)
                .map((UUID partnerId) -> partnerService.findPartner(partnerId)
                        .orElseThrow(() -> new NotFoundResourceException("Partner", partnerId)))
                .collect(Collectors.toList()));

        requireWriteAccessForEachStore(promo.getStores().stream()
                .map(Store::getId)
                .map((UUID storeId) -> storeService.findStore(storeId)
                        .orElseThrow(() -> new NotFoundResourceException("Store", storeId)))
                .collect(Collectors.toList()));

        return promoService.savePromo(promo);
    }

    @Override
    public Optional<Promo> updateLocalPromo(UUID id, Promo promo) {
        log.debug(".updateLocalPromo(id: {})", id);

        Promo found = promoService.findPromo(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        requireWriteAccessForEachPartner(Stream.concat(
                found.getPartners().stream(),
                promo.getPartners().stream()
                        .map(Partner::getId)
                        .map((UUID partnerId) -> partnerService.findPartner(partnerId)
                                .orElseThrow(() -> new NotFoundResourceException("Partner", partnerId))))
                .collect(Collectors.toSet()));

        requireWriteAccessForEachStore(Stream.concat(
                found.getStores().stream(),
                promo.getStores().stream()
                        .map(Store::getId)
                        .map((UUID storeId) -> storeService.findStore(storeId)
                                .orElseThrow(() -> new NotFoundResourceException("Store", storeId))))
                .collect(Collectors.toSet()));

        return promoService.updateLocalPromo(id, promo);
    }

    @Override
    public Optional<UUID> deletePromo(UUID id) {
        log.debug(".deletePromo(id: {})", id);

        Promo found = promoService.findPromo(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        requireWriteAccessForEachPartner(found.getPartners());
        requireWriteAccessForEachStore(found.getStores());

        return promoService.deletePromo(id);
    }

    private void requireReadAccess(Promo promo) {
        Stream.concat(
                promo.getPartners().stream()
                        .map(Partner::getCompany)
                        .map(Company::getId),
                promo.getStores().stream()
                        .map(Store::getPartner)
                        .map(Partner::getCompany)
                        .map(Company::getId))
                .distinct()
                .forEach(companyAuthorityService::checkRead);
    }

    private void requireWriteAccessForEachPartner(Collection<Partner> partners) {
        partners.stream()
                .map(Partner::getCompany)
                .map(Company::getId)
                .distinct()
                .forEach(companyAuthorityService::checkWrite);
    }

    private void requireWriteAccessForEachStore(Collection<Store> stores) {
        stores.stream()
                .map(Store::getPartner)
                .map(Partner::getCompany)
                .map(Company::getId)
                .forEach(companyAuthorityService::checkWrite);
    }
}
