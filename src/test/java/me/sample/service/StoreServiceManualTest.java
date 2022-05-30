package me.sample.service;

import me.sample.repository.PartnerRepository;
import me.sample.repository.StoreRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Partner;
import me.sample.domain.Store;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StoreServiceManualTest {

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Before
    public void setUp() throws Exception {
        storeRepository.deleteAll();
    }

    @Test
    public void shouldSynchronizeStoresCity() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(55.878)
                .lon(37.653)
                .build());

        Thread.sleep(120000);

        Store found = storeRepository.findById(store.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getCity(), is("Москва"));
    }
}
