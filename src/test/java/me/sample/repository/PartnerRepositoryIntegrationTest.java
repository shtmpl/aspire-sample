package me.sample.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Partner;
import me.sample.domain.Promo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PartnerRepositoryIntegrationTest {

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PromoRepository promoRepository;

    @Test
    public void shouldSavePartnerWithAssignedId() throws Exception {
        UUID id = UUID.randomUUID();
        Partner partner = partnerRepository.save(Partner.builder()
                .id(id)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Assigned id: %s. Saved w/ id: %s%n", id, partner.getId());

        assertThat(partner.getId(), is(id));
    }

    @Test
    public void shouldSavePartnerWithGeneratedId() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Saved w/ id: %s%n", partner.getId());

        assertThat(partner.getId(), is(notNullValue()));
    }

    @Test
    public void shouldSavePartnerWithAssignedTime() throws Exception {
        LocalDateTime epoch = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
        Partner partner = partnerRepository.save(Partner.builder()
                .createdDate(epoch)
                .updatedDate(epoch)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Assigned time: %s. Saved w/ createdDate: %s, updatedDate: %s%n", epoch, partner.getCreatedDate(), partner.getUpdatedDate());

        assertThat(partner.getCreatedDate(), is(epoch));
        assertThat(partner.getUpdatedDate(), is(epoch));
    }

    @Test
    public void shouldSavePartnerWithGeneratedTime() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Saved w/ createdDate: %s, updatedDate: %s%n", partner.getCreatedDate(), partner.getUpdatedDate());

        assertThat(partner.getCreatedDate(), is(greaterThanOrEqualTo(now)));
        assertThat(partner.getUpdatedDate(), is(greaterThanOrEqualTo(now)));
    }

    @Test
    public void shouldUpdatePartnerWithAssignedTime() throws Exception {
        LocalDateTime epoch = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
        Partner partner = partnerRepository.save(Partner.builder()
                .createdDate(epoch)
                .updatedDate(epoch)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        LocalDateTime time = epoch.plusDays(1);

        partner = partnerRepository.save(partner.setUpdatedDate(time));

        System.out.printf("Assigned time: %s. Saved w/ createdDate: %s, updatedDate: %s%n", time, partner.getCreatedDate(), partner.getUpdatedDate());

        assertThat(partner.getCreatedDate(), is(epoch));
        assertThat(partner.getUpdatedDate(), is(epoch.plusDays(1)));
    }

    @Test
    public void shouldUpdatePartnerWithGeneratedTime() throws Exception {
        LocalDateTime epoch = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
        Partner partner = partnerRepository.save(Partner.builder()
                .createdDate(epoch)
                .updatedDate(epoch)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        LocalDateTime now = LocalDateTime.now();

        partner = partnerRepository.save(partner.setUpdatedDate(null));

        System.out.printf("Saved w/ createdDate: %s, updatedDate: %s%n", partner.getCreatedDate(), partner.getUpdatedDate());

        assertThat(partner.getCreatedDate(), is(epoch));
        assertThat(partner.getUpdatedDate(), is(greaterThanOrEqualTo(now)));
    }

    @Test
    public void shouldAddPromo() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Promo promo = promoRepository.save(Promo.builder().build());

        partner.getPromos().add(promo);
        partner = partnerRepository.save(partner);

        Set<Promo> promos = partner.getPromos();

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

        Promo promo = promoRepository.save(Promo.builder().build());

        partner.getPromos().add(promo);
        partner = partnerRepository.save(partner);

        partner.getPromos().remove(promo);
        partner = partnerRepository.save(partner);

        Set<Promo> promos = partner.getPromos();

        assertThat(promos.size(), is(0));
    }
}
