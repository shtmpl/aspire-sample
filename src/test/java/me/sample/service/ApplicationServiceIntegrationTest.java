package me.sample.service;

import me.sample.repository.CompanyRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Application;
import me.sample.domain.BadResourceException;
import me.sample.domain.Company;
import me.sample.repository.ApplicationRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationServiceIntegrationTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Before
    public void setUp() throws Exception {
        applicationRepository.deleteAll();
    }

    @Test
    public void shouldSaveApplication() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Company company = companyRepository.save(Company.builder()
                .build());

        Application result = applicationService.saveApplication(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getCreatedDate(), is(greaterThanOrEqualTo(now)));
        assertThat(result.getUpdatedDate(), is(greaterThanOrEqualTo(now)));
    }

    @Test
    public void shouldNotSaveApplicationWithExistingId() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        try {
            applicationService.saveApplication(Application.builder()
                    .id(application.getId())
                    .build());

            fail();
        } catch (BadResourceException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }

    @Test
    public void shouldUpdateApplication() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Application result = applicationService.updateApplication(application.getId(), Application.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build())
                .orElseThrow(AssertionError::new);

        assertThat(result.getId(), is(application.getId()));
        assertThat(result.getCreatedDate(), is(application.getCreatedDate()));
        assertThat(result.getUpdatedDate(), is(greaterThan(application.getUpdatedDate())));
    }
}
