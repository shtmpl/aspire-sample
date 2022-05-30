package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Store;
import me.sample.domain.StoreSpecifications;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.SecuredStoreService;
import me.sample.service.SecurityService;
import me.sample.service.StoreService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@CacheConfig(cacheNames = SecuredStoreServiceImpl.STORE_CACHE)
@Transactional
@Service
public class SecuredStoreServiceImpl implements SecuredStoreService {

    public static final String STORE_CACHE = "store";

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    StoreService storeService;

    @Override
    public Long countStores(Specification<Store> specification) {
        Long userId = securityService.getUserId();
        log.debug(".countStores(), User.id: {}", userId);

        return storeService.countStores(
                StoreSpecifications.partnerCompanyAuthorityUserIdEqualTo(userId)
                        .and(specification));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Store> findStores(Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findStores(), User.id: {}", userId);

        return storeService.findStores(
                StoreSpecifications.partnerCompanyAuthorityUserIdEqualTo(userId),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Store> findStores(Specification<Store> specification, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findStores(), User.id: {}", userId);

        return storeService.findStores(
                StoreSpecifications.partnerCompanyAuthorityUserIdEqualTo(userId)
                        .and(specification),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Store> findStore(UUID id) {
        log.debug(".findStore(id: {})", id);

        Store found = storeService.findStore(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getPartner().getCompany().getId());

        return Optional.of(found);
    }

    @Override
    public Store saveStore(Store data) {
        log.debug(".saveStore()");

        companyAuthorityService.checkWrite(data.getPartner().getCompany().getId());

        return storeService.saveStore(data);
    }

    @Override
    public Optional<Store> updateStore(UUID id, Store data) {
        log.debug(".updateStore(id: {})", id);

        Store found = storeService.findStore(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(data.getPartner().getCompany().getId());
        companyAuthorityService.checkWrite(found.getPartner().getCompany().getId());

        return storeService.updateLocalStore(id, data);
    }

    @Override
    public Optional<UUID> deleteStore(UUID id) {
        log.debug(".deleteStore(id: {})", id);

        Store found = storeService.findStore(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(found.getPartner().getCompany().getId());

        return storeService.deleteStore(id);
    }
}
