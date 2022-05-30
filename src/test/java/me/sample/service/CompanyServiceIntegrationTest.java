package me.sample.service;

import me.sample.repository.CompanyRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.BadResourceException;
import me.sample.domain.Company;

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
public class CompanyServiceIntegrationTest {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Before
    public void setUp() throws Exception {
//        companyRepository.deleteAll();
    }

    @Test
    public void shouldSaveCompany() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Company result = companyService.saveCompany(Company.builder()
                .build());

        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getCreatedDate(), is(greaterThanOrEqualTo(now)));
        assertThat(result.getUpdatedDate(), is(greaterThanOrEqualTo(now)));
    }

    @Test
    public void shouldNotSaveCompanyWithExistingId() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        try {
            companyService.saveCompany(Company.builder()
                    .id(company.getId())
                    .build());

            fail();
        } catch (BadResourceException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }

    @Test
    public void shouldUpdateCompany() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        Company result = companyService.updateCompany(company.getId(), Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build())
                .orElseThrow(AssertionError::new);

        assertThat(result.getId(), is(company.getId()));
        assertThat(result.getCreatedDate(), is(company.getCreatedDate()));
        assertThat(result.getUpdatedDate(), is(greaterThan(company.getUpdatedDate())));
    }
}
