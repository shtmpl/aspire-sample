package me.sample.service;

import me.sample.Resources;
import me.sample.repository.CampaignRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.repository.CampaignClientRepository;
import me.sample.web.rest.ContentTypes;

import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CampaignServicePerformanceTest {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignClientRepository campaignClientRepository;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldUpdateCampaignClientsFromCsv() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.println("Updating campaign clients...");
        long time = System.nanoTime();
        campaignService.updateCampaignClientsFromData(
                campaign,
                ContentTypes.CSV,
                Base64.getEncoder().encodeToString(Resources.slurpBytes("client/client-ids.csv")));

        System.out.printf("Elapsed time: %s%n", Duration.ofNanos(System.nanoTime() - time));
    }
}
