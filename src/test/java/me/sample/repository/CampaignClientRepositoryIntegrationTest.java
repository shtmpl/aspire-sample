package me.sample.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignClient;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CampaignClientRepositoryIntegrationTest {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignClientRepository campaignClientRepository;

    @Before
    public void setUp() throws Exception {
        campaignClientRepository.deleteAll();
    }

    @Test
    public void shouldSaveCampaignClient() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        String clientId = String.valueOf(UUID.randomUUID());
        CampaignClient result = campaignClientRepository.save(CampaignClient.builder()
                .campaign(campaign)
                .build()
                .setClientId(clientId));

        assertThat(result.getId().getCampaignId(), is(campaign.getId()));
        assertThat(result.getId().getClientId(), is(clientId));
    }
}
