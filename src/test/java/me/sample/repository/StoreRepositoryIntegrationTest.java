package me.sample.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignState;
import me.sample.domain.Partner;
import me.sample.domain.Promo;
import me.sample.domain.Store;
import me.sample.domain.StoreSpecifications;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StoreRepositoryIntegrationTest {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PromoRepository promoRepository;

    @Before
    public void setUp() throws Exception {
        storeRepository.deleteAll();
    }

    @Test
    public void shouldFindStoresWhereCityIsBlank() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store1 = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .city(null)
                .build());

        Store store2 = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .city("")
                .build());

        Store store3 = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .city("    ")
                .build());

        Store store4 = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .city("X")
                .build());


        List<Store> stores = storeRepository.findAll(StoreSpecifications.cityIsBlank());


        assertThat(stores.stream()
                        .map(Store::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(
                        store1.getId(),
                        store2.getId(),
                        store3.getId()));
    }

    @Test
    public void shouldFindStoresWhereCityLike() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store1 = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .city("Санкт-Петербург")
                .build());

        Store store2 = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .city("САНКТ-ПЕТЕРБУРГ")
                .build());

        Store store3 = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .city("Санкт-Петербург и Ленинградская область")
                .build());

        Store store4 = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .city("г. Санкт-Петербург")
                .build());

        Store store5 = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .city("45 км Автодороги Санкт-Петербург-Псков")
                .build());


        List<Store> stores = storeRepository.findAll(StoreSpecifications.cityLike("Санкт-Петербург"));


        assertThat(stores.stream()
                        .map(Store::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(
                        store1.getId(),
                        store2.getId(),
                        store3.getId(),
                        store4.getId(),
                        store5.getId()));
    }

    @Test
    public void shouldFindStoresWithDistanceToGeoposition() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .state(CampaignState.RUNNING)
                .name(String.valueOf(UUID.randomUUID()))
                .radius(1000L)
                .build());

        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        List<Map.Entry<Store, Double>> results = storeRepository.findAllNeighbouringWithDistanceToGeoposition(null, 42.01);

        assertThat(results, not(empty()));

        System.out.printf("Found:%n");
        results.forEach((Map.Entry<Store, Double> entry) -> {
            Store found = entry.getKey();
            Double distanceToGeoposition = entry.getValue();
            System.out.printf("Store id: %s, distanceToGeoposition: %s%n", found.getId(), distanceToGeoposition);

            assertThat(found, is(notNullValue()));
            assertThat(distanceToGeoposition, is(notNullValue()));
        });
    }

    @Test
    public void shouldSaveWithAssignedId() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        UUID id = UUID.randomUUID();
        Store store = storeRepository.save(Store.builder()
                .id(id)
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Assigned id: %s. Saved w/ id: %s%n", id, store.getId());

        assertThat(store.getId(), is(id));
    }

    @Test
    public void shouldSaveWithGeneratedId() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Saved w/ id: %s%n", store.getId());

        assertThat(store.getId(), is(notNullValue()));
    }

    @Test
    public void shouldAddPromo() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        Promo promo = promoRepository.save(Promo.builder().build());

        store.getPromos().add(promo);
        store = storeRepository.save(store);

        Set<Promo> promos = store.getPromos();

        assertThat(promos.size(), is(1));
        promos.forEach((Promo it) -> {
            assertThat(it.getId(), is(notNullValue()));
        });
    }

    @Test
    public void shouldRemovePromo() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        Promo promo = promoRepository.save(Promo.builder().build());

        store.getPromos().add(promo);
        store = storeRepository.save(store);

        store.getPromos().remove(promo);
        store = storeRepository.save(store);

        Set<Promo> storePromos = store.getPromos();

        assertThat(storePromos.size(), is(0));
    }
}
