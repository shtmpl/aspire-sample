package me.sample.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Company;
import me.sample.domain.Partner;
import me.sample.domain.Promo;
import me.sample.domain.Store;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PromoRepositoryIntegrationTest {

    @Autowired
    private PromoRepository promoRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    public void shouldFindPromosByPartnersAndStores() throws Exception {
        Promo promo = promoRepository.save(Promo.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Company company = companyRepository.save(Company.builder()
                .build());

        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .promos(Stream.of(promo).collect(Collectors.toSet()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .promos(Stream.of(promo).collect(Collectors.toSet()))
                .build());

        Set<Promo> promos = promoRepository.findAllByPartnersInOrStoresIn(
                Stream.of(partner).collect(Collectors.toSet()),
                Stream.of(store).collect(Collectors.toSet()));

        assertThat(promos, containsInAnyOrder(promo));
    }

    @Test
    public void shouldSavePromoWithAssignedId() throws Exception {
        UUID id = UUID.randomUUID();
        Promo promo = promoRepository.save(Promo.builder()
                .id(id)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Assigned id: %s. Saved w/ id: %s%n", id, promo.getId());

        assertThat(promo.getId(), is(id));
    }

    @Test
    public void shouldSavePromoWithGeneratedId() throws Exception {
        Promo promo = promoRepository.save(Promo.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Saved w/ id: %s%n", promo.getId());

        assertThat(promo.getId(), is(notNullValue()));
    }
}
