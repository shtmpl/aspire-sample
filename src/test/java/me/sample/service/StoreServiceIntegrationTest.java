package me.sample.service;

import me.sample.repository.PartnerRepository;
import me.sample.repository.StoreRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.BadResourceException;
import me.sample.domain.Partner;
import me.sample.domain.Specifications;
import me.sample.domain.Store;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StoreServiceIntegrationTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Before
    public void setUp() throws Exception {
        storeRepository.deleteAll();
    }

    @Test
    public void shouldCountStores() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Long result = storeService.countStores(Specifications.any());

        assertThat(result, is(1L));
    }

    @Test
    public void shouldFindStoreCities() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Stream.of(
                null,
                "",
                "    ",
                "Санкт-Петербург",
                "г. Санкт-Петербург",
                "САНКТ-ПЕТЕРБУРГ",
                "Санкт-Петербург и Ленинградская область",
                "45 км Автодороги Санкт-Петербург-Псков")
                .forEach((String city) ->
                        storeRepository.save(Store.builder()
                                .partner(partner)
                                .name(String.valueOf(UUID.randomUUID()))
                                .city(city)
                                .build()));


        Set<String> result = storeService.findStoreCities();


        assertThat(result,
                containsInAnyOrder(
                        "Санкт-Петербург",
                        "Санкт-Петербург И Ленинградская Область",
                        "45 Км Автодороги Санкт-Петербург-Псков"));
    }

    @Test
    public void shouldSaveStore() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Partner partner = partnerRepository.save(Partner.builder()
                .build());

        Store result = storeService.saveStore(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getCreatedDate(), is(greaterThanOrEqualTo(now)));
        assertThat(result.getUpdatedDate(), is(result.getCreatedDate()));
    }

    @Test
    public void shouldNotSaveStoreWithExistingId() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        try {
            storeService.saveStore(Store.builder()
                    .id(store.getId())
                    .build());

            fail();
        } catch (BadResourceException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }

    @Test
    public void shouldUpdateLocalStore() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store result = storeService.updateLocalStore(store.getId(), Store.builder()
                .build())
                .orElseThrow(AssertionError::new);

        assertThat(result.getId(), is(store.getId()));
        assertThat(result.getCreatedDate(), is(store.getCreatedDate()));
        assertThat(result.getUpdatedDate(), is(greaterThan(store.getUpdatedDate())));
    }
}
