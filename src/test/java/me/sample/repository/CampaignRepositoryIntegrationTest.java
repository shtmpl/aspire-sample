package me.sample.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignState;
import me.sample.domain.Company;

import java.util.EnumSet;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CampaignRepositoryIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Test
    public void shouldSaveCampaignWithStates() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        EnumSet.allOf(CampaignState.class).forEach((CampaignState state) ->
                campaignRepository.save(Campaign.builder()
                        .company(company)
                        .state(state)
                        .name(String.valueOf(UUID.randomUUID()))
                        .build()));
    }
}
