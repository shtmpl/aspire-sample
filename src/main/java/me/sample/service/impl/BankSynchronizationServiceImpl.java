package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.BankSynchronizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.NotFoundResourceException;
import me.sample.gateway.bank.BankApiMapper;
import me.sample.gateway.bank.BankGateway;
import me.sample.gateway.bank.response.LayoutPage;
import me.sample.gateway.bank.response.ResponseCategory;
import me.sample.gateway.bank.response.ResponsePaging;
import me.sample.gateway.bank.response.ResponsePartner;
import me.sample.gateway.bank.response.ResponsePromo;
import me.sample.gateway.bank.response.ResponseShop;
import me.sample.domain.Category;
import me.sample.domain.Partner;
import me.sample.domain.Promo;
import me.sample.domain.Store;
import me.sample.domain.StoreState;
import me.sample.repository.PartnerRepository;
import me.sample.repository.PromoRepository;
import me.sample.repository.StoreRepository;
import me.sample.service.CategoryService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class BankSynchronizationServiceImpl implements BankSynchronizationService {

    private static final Integer DEFAULT_PAGE_SIZE = 100;

    @NonFinal
    BankSynchronizationService self;

    BankGateway bankGateway;
    BankApiMapper bankApiMapper;

    CategoryService categoryService;

    PartnerRepository partnerRepository;
    StoreRepository storeRepository;
    PromoRepository promoRepository;

    @Autowired
    public void setSelf(@Lazy BankSynchronizationService self) {
        this.self = self;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void syncResources() {
        syncCategories();
        syncPartnersAndAssociations();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void syncCategories() {
        log.info("Synchronizing categories...");

        LayoutPage<ResponseCategory> page = bankGateway.indexCategories(PageRequest.of(0, DEFAULT_PAGE_SIZE));
        ResponsePaging paging = page.getPaging();
        int totalPages = paging.getTotalPages();
        long totalElements = paging.getTotalElements();

        log.info("Found {} categories for import", totalElements);

        log.debug("Processing page {} of {}", 1, totalPages);
        page.getData().forEach((ResponseCategory response) ->
                saveOrUpdateCategoryFromResponse(response));

        IntStream.range(1, totalPages)
                .peek((int pageNumber) ->
                        log.debug("Processing page {} of {}", pageNumber + 1, totalPages))
                .forEach((int pageNumber) ->
                        bankGateway.indexCategories(PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE)).getData()
                                .forEach((ResponseCategory response) ->
                                        saveOrUpdateCategoryFromResponse(response)));

        log.info("Synchronized categories");
    }

    private void saveOrUpdateCategoryFromResponse(ResponseCategory response) {
        Category category = bankApiMapper.mapToCategory(response);

        categoryService.findCategory(category.getId())
                .flatMap((Category found) -> categoryService.updateImportedCategory(category.getId(), category))
                .orElseGet(() -> categoryService.saveCategory(category));
    }

    @Override
    public void syncPartnersAndAssociations() {
        log.info("Performing synchronization for partners...");
        LayoutPage<ResponsePartner> page = bankGateway.indexPartners(PageRequest.of(0, DEFAULT_PAGE_SIZE));
        ResponsePaging paging = page.getPaging();
        log.info("Found {} partners for import", paging.getTotalElements());

        syncPartnerAndAssociationsForPage(page);

        IntStream.range(1, paging.getTotalPages())
                .mapToObj((int pageNumber) ->
                        bankGateway.indexPartners(PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE)))
                .forEach(this::syncPartnerAndAssociationsForPage);

        log.info("Performed synchronization for partners");
    }

    private void syncPartnerAndAssociationsForPage(LayoutPage<ResponsePartner> page) {
        ResponsePaging paging = page.getPaging();
        log.info("Processing page {} of {}", paging.getPageNumber() + 1, paging.getTotalPages());

        List<ResponsePartner> data = page.getData();
        if (data == null) {
            log.warn("No data available for page: {}", paging.getPageNumber());

            return;
        }

        for (ResponsePartner response : data) {
            Partner partner = bankApiMapper.mapToPartner(response);

            Partner found = partnerRepository.findById(partner.getId())
                    .orElse(null);
            if (found != null &&
                    found.getUpdatedDate() != null &&
                    (found.getUpdatedDate().isEqual(partner.getUpdatedDate()) || found.getUpdatedDate().isAfter(partner.getUpdatedDate()))) {
                log.debug("Skipped synchronization for partner id: {}. Found.updatedDate: {}. Updated.updatedDate: {}",
                        partner.getId(),
                        found.getUpdatedDate(),
                        partner.getUpdatedDate());

                continue;
            }

            try {
                syncPartnerAndAssociations(partner);
            } catch (Exception exception) {
                log.error("Error processing synchronization data for partner id: {}", partner.getId(), exception);
            }
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public Optional<Partner> syncPartnerAndAssociations(UUID id) {
        log.info("Performing synchronization for partner id: {}...", id);

        Partner partner = fetchPartner(id)
                .orElse(null);
        if (partner == null) {
            log.warn("No synchronization data available for partner id: {}", id);

            return Optional.empty();
        }

        Partner found = partnerRepository.findById(id)
                .orElse(null);
        if (found != null &&
                found.getUpdatedDate() != null &&
                (found.getUpdatedDate().isEqual(partner.getUpdatedDate()) || found.getUpdatedDate().isAfter(partner.getUpdatedDate()))) {
            log.debug("Skipped synchronization for partner id: {}. Found.updatedDate: {}. Updated.updatedDate: {}",
                    id,
                    found.getUpdatedDate(),
                    partner.getUpdatedDate());

            return Optional.of(found);
        }

        Partner result = syncPartnerAndAssociations(partner);

        log.info("Performed synchronization for partner id: {}", id);

        return Optional.of(result);
    }

    private Partner syncPartnerAndAssociations(Partner partner) {
        Set<Store> stores = partner.getStores().stream()
                .map(Store::getId)
                .map((UUID storeId) -> fetchStore(storeId)
                        .orElseThrow(() -> new NotFoundResourceException("Store", storeId)))
                .collect(Collectors.toSet());

        Set<Promo> promos = Stream.concat(
                partner.getPromos().stream(),
                stores.stream()
                        .map(Store::getPromos)
                        .flatMap(Collection::stream))
                .map(Promo::getId)
                .distinct()
                .map((UUID promoId) -> fetchPromo(promoId)
                        .orElseThrow(() -> new NotFoundResourceException("Promo", promoId)))
                .collect(Collectors.toSet());

        return self.savePartnerAndAssociations(partner, stores, promos);
    }

    private Optional<Partner> fetchPartner(UUID id) {
        ResponsePartner response = bankGateway.showPartner(id)
                .orElse(null);
        if (response == null) {
            return Optional.empty();
        }

        return Optional.of(bankApiMapper.mapToPartner(response));
    }

    private Optional<Store> fetchStore(UUID id) {
        ResponseShop response = bankGateway.showShop(id)
                .orElse(null);
        if (response == null) {
            return Optional.empty();
        }

        return Optional.of(bankApiMapper.mapToStore(response));
    }

    private Optional<Promo> fetchPromo(UUID id) {
        ResponsePromo response = bankGateway.showPromo(id)
                .orElse(null);
        if (response == null) {
            return Optional.empty();
        }

        return Optional.of(bankApiMapper.mapToPromo(response));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Partner savePartnerAndAssociations(Partner partner, Set<Store> stores, Set<Promo> promos) {
        UUID partnerId = partner.getId();

        log.info("Saving synchronization data for partner id: {}", partnerId);

        promoRepository.saveAll(promos);
        log.debug("Saved promos count: {}", promos.size());

        try (Stream<Store> associatedStores = storeRepository.findAllByPartnerId(partnerId)) {
            stores.addAll(associatedStores
                    .filter((Store associatedStore) -> !stores.contains(associatedStore))
                    .map((Store associatedStore) -> associatedStore.setState(StoreState.INACTIVE))
                    .collect(Collectors.toSet()));
        }

        Partner result = partnerRepository.save(partner.setStores(stores));
        log.debug("Saved stores count: {}", stores.size());

        return result;
    }
}
