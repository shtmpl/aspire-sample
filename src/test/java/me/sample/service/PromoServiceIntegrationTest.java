package me.sample.service;

import me.sample.repository.CompanyRepository;
import me.sample.repository.PartnerRepository;
import me.sample.repository.PromoRepository;
import me.sample.repository.StoreRepository;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.BadResourceException;
import me.sample.domain.Company;
import me.sample.domain.Partner;
import me.sample.domain.Promo;
import me.sample.domain.Source;
import me.sample.domain.Store;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PromoServiceIntegrationTest {

    @Autowired
    private PromoService promoService;

    @Autowired
    private PromoRepository promoRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    public void shouldSavePromo() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Promo result = promoService.savePromo(Promo.builder()
                .build());

        assertThat(result.getId(), is(CoreMatchers.notNullValue()));
        assertThat(result.getCreatedDate(), is(greaterThanOrEqualTo(now)));
        assertThat(result.getUpdatedDate(), is(result.getCreatedDate()));
    }

    @Test
    public void shouldNotSavePromoWithExistingId() throws Exception {
        Promo promo = promoService.savePromo(Promo.builder()
                .build());

        try {
            promoService.savePromo(Promo.builder()
                    .id(promo.getId())
                    .build());

            fail();
        } catch (BadResourceException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }

    @Test
    public void shouldSavePromoWithPartnerAssociation() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Promo promo = promoService.savePromo(Promo.builder()
                .partners(Stream.of(partner).collect(Collectors.toSet()))
                .build());

        assertThat(promo.getId(), is(notNullValue()));

        Set<Promo> partnerPromos = partnerRepository.findWithPromosById(partner.getId())
                .map(Partner::getPromos)
                .orElseThrow(AssertionError::new);

        assertThat(partnerPromos, containsInAnyOrder(promo));
    }

    @Test
    public void shouldSavePromoWithStoreAssociation() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        Promo promo = promoService.savePromo(Promo.builder()
                .stores(Stream.of(store).collect(Collectors.toSet()))
                .build());

        assertThat(promo.getId(), is(notNullValue()));

        Set<Promo> storePromos = storeRepository.findWithPromosById(store.getId())
                .map(Store::getPromos)
                .orElseThrow(AssertionError::new);

        assertThat(storePromos, containsInAnyOrder(promo));
    }

    @Test
    public void shouldUpdatePromo() throws Exception {
        Promo promo = promoRepository.save(Promo.builder()
                .source(Source.LOCAL)
                .build());

        promo = promoService.updateLocalPromo(promo.getId(), Promo.builder()
                .source(Source.LOCAL)
                .build())
                .orElseThrow(AssertionError::new);

        assertThat(promo.getId(), is(notNullValue()));
    }

    @Test
    public void shouldUpdatePromoWithPartnerAssociation() throws Exception {
        Promo promo = promoRepository.save(Promo.builder()
                .source(Source.LOCAL)
                .build());

        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        UUID firstPartnerId = UUID.randomUUID();
        Partner firstPartner = partnerRepository.save(Partner.builder()
                .company(company)
                .id(firstPartnerId)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        firstPartner.getPromos().add(promo);
        partnerRepository.save(firstPartner);

        UUID otherPartnerId = UUID.randomUUID();
        Partner otherPartner = partnerRepository.save(Partner.builder()
                .company(company)
                .id(otherPartnerId)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        promo = promoService.updateLocalPromo(promo.getId(),
                Promo.builder()
                        .source(Source.LOCAL)
                        .partners(Stream.of(otherPartner).collect(Collectors.toSet()))
                        .build())
                .orElseThrow(AssertionError::new);

        assertThat(promo.getId(), is(notNullValue()));

        Set<Promo> firstPartnerPromos = partnerRepository.findWithPromosById(firstPartnerId)
                .map(Partner::getPromos)
                .orElseThrow(AssertionError::new);

        assertThat(firstPartnerPromos, empty());

        Set<Promo> otherPartnerPromos = partnerRepository.findWithPromosById(otherPartnerId)
                .map(Partner::getPromos)
                .orElseThrow(AssertionError::new);

        assertThat(otherPartnerPromos, containsInAnyOrder(promo));
    }

    @Test
    public void shouldUpdatePromoWithStoreAssociation() throws Exception {
        Promo promo = promoRepository.save(Promo.builder()
                .source(Source.LOCAL)
                .build());

        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        UUID firstStoreId = UUID.randomUUID();
        Store firstStore = storeRepository.save(Store.builder()
                .partner(partner)
                .id(firstStoreId)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        firstStore.getPromos().add(promo);
        storeRepository.save(firstStore);

        UUID otherStoreId = UUID.randomUUID();
        Store otherStore = storeRepository.save(Store.builder()
                .partner(partner)
                .id(otherStoreId)
                .name(String.format("store.%s", otherStoreId))
                .lat(42.0)
                .lon(42.0)
                .build());

        promo = promoService.updateLocalPromo(promo.getId(),
                Promo.builder()
                        .source(Source.LOCAL)
                        .stores(Stream.of(otherStore).collect(Collectors.toSet()))
                        .build())
                .orElseThrow(AssertionError::new);

        assertThat(promo.getId(), is(notNullValue()));

        Set<Promo> firstStorePromos = storeRepository.findWithPromosById(firstStoreId)
                .map(Store::getPromos)
                .orElseThrow(AssertionError::new);

        assertThat(firstStorePromos, empty());

        Set<Promo> otherStorePromos = storeRepository.findWithPromosById(otherStoreId)
                .map(Store::getPromos)
                .orElseThrow(AssertionError::new);

        assertThat(otherStorePromos, containsInAnyOrder(promo));
    }
}
