package me.sample.service;

import me.sample.repository.CompanyRepository;
import me.sample.repository.PartnerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.BadResourceException;
import me.sample.domain.Partner;
import me.sample.domain.Source;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PartnerServiceIntegrationTest {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Before
    public void setUp() throws Exception {
        partnerRepository.deleteAll();
    }

    @Test
    public void shouldSaveLocalPartner() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Partner result = partnerService.savePartner(Partner.builder()
                .build());

        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getCreatedDate(), is(greaterThanOrEqualTo(now)));
        assertThat(result.getUpdatedDate(), is(result.getCreatedDate()));
    }

    @Test
    public void shouldNotSavePartnerWithExistingId() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .build());

        try {
            partnerService.savePartner(Partner.builder()
                    .id(partner.getId())
                    .build());

            fail();
        } catch (BadResourceException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }

    @Test
    public void shouldUpdateLocalPartner() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .source(Source.LOCAL)
                .build());

        Partner result = partnerService.updateLocalPartner(partner.getId(), Partner.builder()
                .build())
                .orElseThrow(AssertionError::new);

        assertThat(result.getId(), is(partner.getId()));
        assertThat(result.getCreatedDate(), is(partner.getCreatedDate()));
        assertThat(result.getUpdatedDate(), is(greaterThan(partner.getUpdatedDate())));
    }
}
