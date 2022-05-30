package me.sample.service;

import me.sample.repository.PartnerRepository;
import me.sample.repository.PromoRepository;
import me.sample.repository.StoreRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.gateway.bank.BankGateway;
import me.sample.gateway.bank.response.ResponseCoordinates;
import me.sample.gateway.bank.response.ResponsePartner;
import me.sample.gateway.bank.response.ResponsePromo;
import me.sample.gateway.bank.response.ResponseShop;
import me.sample.gateway.bank.response.ResponseShopLocation;
import me.sample.domain.Company;
import me.sample.domain.Partner;
import me.sample.domain.PartnerState;
import me.sample.domain.Promo;
import me.sample.domain.PromoState;
import me.sample.domain.Source;
import me.sample.domain.Store;
import me.sample.domain.StoreState;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BankSynchronizationServiceIntegrationTest {

    @Autowired
    private BankSynchronizationService bankSynchronizationService;

    @MockBean
    private BankGateway bankGateway;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PromoRepository promoRepository;

    @Test
    public void shouldSavePartnerAndAssociations() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        UUID partnerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID promoId = UUID.randomUUID();

        ResponsePartner responsePartner = ResponsePartner.builder()
                .id(partnerId)
                .creationDate(now)
                .isActive(true)
                .shopsId(Stream.of(storeId).collect(Collectors.toList()))
                .promos(Stream.of(promoId).collect(Collectors.toList()))
                .build();

        when(bankGateway.showPartner(partnerId))
                .thenReturn(Optional.of(responsePartner));

        ResponseShop responseShop = ResponseShop.builder()
                .id(storeId)
                .partnerId(partnerId)
                .location(ResponseShopLocation.builder()
                        .coordinates(ResponseCoordinates.builder()
                                .latitude(42.0)
                                .longitude(42.0)
                                .build())
                        .build())
                .promos(Stream.of(promoId).collect(Collectors.toList()))
                .build();

        when(bankGateway.showShop(storeId))
                .thenReturn(Optional.of(responseShop));


        ResponsePromo responsePromo = ResponsePromo.builder()
                .id(promoId)
                .build();

        when(bankGateway.showPromo(promoId))
                .thenReturn(Optional.of(responsePromo));


        bankSynchronizationService.syncPartnerAndAssociations(partnerId);


        Partner partner = partnerRepository.findWithPromosById(partnerId)
                .orElseThrow(AssertionError::new);

        assertThat(partner.getSource(), is(Source.IMPORT_BANK));
        assertThat(partner.getCreatedDate(), is(now));
        assertThat(partner.getUpdatedDate(), is(now));
        assertThat(partner.getState(), is(PartnerState.ACTIVE));
        assertThat(partner.getPromos().stream()
                        .map(Promo::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(promoId));

        Store store = storeRepository.findWithPromosById(storeId)
                .orElseThrow(AssertionError::new);

        assertThat(store.getSource(), is(Source.IMPORT_BANK));
        assertThat(store.getState(), is(StoreState.ACTIVE));
        assertThat(store.getPromos().stream()
                        .map(Promo::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(promoId));

        Promo promo = promoRepository.findById(promoId)
                .orElseThrow(AssertionError::new);

        assertThat(promo.getSource(), is(Source.IMPORT_BANK));
        assertThat(promo.getState(), is(PromoState.ACTIVE));
    }

    @Test
    public void shouldUpdatePartnerAndAssociations() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        UUID partnerId = UUID.randomUUID();

        UUID oldStoreId = UUID.randomUUID();
        UUID oldPromoId = UUID.randomUUID();

        Partner partner = partnerRepository.save(Partner.builder()
                .id(partnerId)
                .createdDate(now)
                .updatedDate(now)
                .company(Company.builder()
                        .id(UUID.fromString(BankSynchronizationService.COMPANY_ID_BANK))
                        .build())
                .source(Source.IMPORT_BANK)
                .state(PartnerState.ACTIVE)
                .build());

        Store oldStore = storeRepository.save(Store.builder()
                .id(oldStoreId)
                .partner(partner)
                .source(Source.IMPORT_BANK)
                .state(StoreState.ACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        Promo oldPromo = promoRepository.save(Promo.builder()
                .id(oldPromoId)
                .source(Source.IMPORT_BANK)
                .state(PromoState.ACTIVE)
                .build());

        partnerRepository.save(partner.setPromos(Stream.of(oldPromo).collect(Collectors.toSet())));
        storeRepository.save(oldStore.setPromos(Stream.of(oldPromo).collect(Collectors.toSet())));


        UUID newStoreId = UUID.randomUUID();
        UUID newPromoId = UUID.randomUUID();

        ResponsePartner responsePartner = ResponsePartner.builder()
                .id(partnerId)
                .creationDate(now)
                .lastUpdateDate(now.plusDays(1))
                .isActive(true)
                .shopsId(Stream.of(newStoreId).collect(Collectors.toList()))
                .promos(Stream.of(newPromoId).collect(Collectors.toList()))
                .build();

        when(bankGateway.showPartner(partnerId))
                .thenReturn(Optional.of(responsePartner));

        ResponseShop responseShop = ResponseShop.builder()
                .id(newStoreId)
                .partnerId(partnerId)
                .location(ResponseShopLocation.builder()
                        .coordinates(ResponseCoordinates.builder()
                                .latitude(42.0)
                                .longitude(42.0)
                                .build())
                        .build())
                .promos(Stream.of(newPromoId).collect(Collectors.toList()))
                .build();

        when(bankGateway.showShop(newStoreId))
                .thenReturn(Optional.of(responseShop));


        ResponsePromo responsePromo = ResponsePromo.builder()
                .id(newPromoId)
                .build();

        when(bankGateway.showPromo(newPromoId))
                .thenReturn(Optional.of(responsePromo));


        bankSynchronizationService.syncPartnerAndAssociations(partnerId);


        partner = partnerRepository.findWithStoresAndPromosById(partnerId)
                .orElseThrow(AssertionError::new);

        assertThat(partner.getSource(), is(Source.IMPORT_BANK));
        assertThat(partner.getCreatedDate(), is(now));
        assertThat(partner.getUpdatedDate(), is(now.plusDays(1)));
        assertThat(partner.getState(), is(PartnerState.ACTIVE));
        assertThat(partner.getStores().stream()
                        .map(Store::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(oldStoreId, newStoreId));
        assertThat(partner.getPromos().stream()
                        .map(Promo::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(newPromoId));

        oldStore = storeRepository.findWithPromosById(oldStoreId)
                .orElseThrow(AssertionError::new);

        assertThat(oldStore.getSource(), is(Source.IMPORT_BANK));
        assertThat(oldStore.getState(), is(StoreState.INACTIVE));
        assertThat(oldStore.getPromos().stream()
                        .map(Promo::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(oldPromoId));

        Store newStore = storeRepository.findWithPromosById(newStoreId)
                .orElseThrow(AssertionError::new);

        assertThat(newStore.getSource(), is(Source.IMPORT_BANK));
        assertThat(newStore.getState(), is(StoreState.ACTIVE));
        assertThat(newStore.getPromos().stream()
                        .map(Promo::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(newPromoId));

        oldPromo = promoRepository.findById(oldPromoId)
                .orElseThrow(AssertionError::new);

        assertThat(oldPromo.getSource(), is(Source.IMPORT_BANK));
        assertThat(oldPromo.getState(), is(PromoState.ACTIVE)); // FIXME: Deactivation for Promo is not implemented

        Promo newPromo = promoRepository.findById(newPromoId)
                .orElseThrow(AssertionError::new);

        assertThat(newPromo.getSource(), is(Source.IMPORT_BANK));
        assertThat(newPromo.getState(), is(PromoState.ACTIVE));
    }

    @Test
    public void shouldUpdatePartnerAndAssociationsWithoutStoreAndPromo() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        UUID partnerId = UUID.randomUUID();

        UUID oldStoreId = UUID.randomUUID();
        UUID oldPromoId = UUID.randomUUID();

        Partner partner = partnerRepository.save(Partner.builder()
                .id(partnerId)
                .createdDate(now)
                .updatedDate(now)
                .company(Company.builder()
                        .id(UUID.fromString(BankSynchronizationService.COMPANY_ID_BANK))
                        .build())
                .source(Source.IMPORT_BANK)
                .state(PartnerState.ACTIVE)
                .build());

        Store oldStore = storeRepository.save(Store.builder()
                .id(oldStoreId)
                .partner(partner)
                .source(Source.IMPORT_BANK)
                .state(StoreState.ACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        Promo oldPromo = promoRepository.save(Promo.builder()
                .id(oldPromoId)
                .source(Source.IMPORT_BANK)
                .state(PromoState.ACTIVE)
                .build());

        partnerRepository.save(partner.setPromos(Stream.of(oldPromo).collect(Collectors.toSet())));
        storeRepository.save(oldStore.setPromos(Stream.of(oldPromo).collect(Collectors.toSet())));


        ResponsePartner responsePartner = ResponsePartner.builder()
                .id(partnerId)
                .creationDate(now)
                .lastUpdateDate(now.plusDays(1))
                .isActive(true)
                .shopsId(Collections.emptyList())
                .promos(Collections.emptyList())
                .build();

        when(bankGateway.showPartner(partnerId))
                .thenReturn(Optional.of(responsePartner));


        bankSynchronizationService.syncPartnerAndAssociations(partnerId);


        partner = partnerRepository.findWithStoresAndPromosById(partnerId)
                .orElseThrow(AssertionError::new);

        assertThat(partner.getSource(), is(Source.IMPORT_BANK));
        assertThat(partner.getCreatedDate(), is(now));
        assertThat(partner.getUpdatedDate(), is(now.plusDays(1)));
        assertThat(partner.getState(), is(PartnerState.ACTIVE));
        assertThat(partner.getStores().stream()
                        .map(Store::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(oldStoreId));
        assertThat(partner.getPromos(), is(empty()));

        oldStore = storeRepository.findWithPromosById(oldStoreId)
                .orElseThrow(AssertionError::new);

        assertThat(oldStore.getSource(), is(Source.IMPORT_BANK));
        assertThat(oldStore.getState(), is(StoreState.INACTIVE));
        assertThat(oldStore.getPromos().stream()
                        .map(Promo::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(oldPromoId));

        oldPromo = promoRepository.findById(oldPromoId)
                .orElseThrow(AssertionError::new);

        assertThat(oldPromo.getSource(), is(Source.IMPORT_BANK));
        assertThat(oldPromo.getState(), is(PromoState.ACTIVE)); // FIXME: Deactivation for Promo is not implemented
    }
}
