package me.sample.service;

import me.sample.repository.PartnerRepository;
import me.sample.repository.StoreRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.gateway.dadata.DadataGateway;
import me.sample.domain.Partner;
import me.sample.domain.Store;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "logging.level.me.sample=INFO"
})
public class StoreServicePerformanceTest {

    @Autowired
    private StoreService storeService;

    @MockBean
    private DadataGateway dadataGateway;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Before
    public void setUp() throws Exception {
        storeRepository.deleteAll();
    }

    @Test
    public void shouldSyncStoreCities() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .build());

        for (int batch = 0; batch < 10; batch += 1) {
            List<Store> stores = IntStream.range(0, 100000)
                    .mapToObj((int x) -> Store.builder()
                            .partner(partner)
                            .name(String.valueOf(UUID.randomUUID()))
                            .lat(42.0)
                            .lon(42.0)
                            .build())
                    .collect(Collectors.toList());

            System.out.println("Saving stores...");
            storeRepository.saveAll(stores);
        }


        Mockito.when(dadataGateway.findCityByCoordinates(Mockito.anyDouble(), Mockito.anyDouble()))
                .thenReturn(Optional.of("X"));


        System.out.println("Sync'ing store cities...");
        long time = System.nanoTime();
        storeService.syncStoreCities();

        System.out.printf("Elapsed time: %s%n", Duration.ofNanos(System.nanoTime() - time));
    }
}
