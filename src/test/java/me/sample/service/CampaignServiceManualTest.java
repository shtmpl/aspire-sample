package me.sample.service;

import me.sample.repository.CampaignRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignState;
import me.sample.domain.NotificationStatistic;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CampaignServiceManualTest {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Test
    public void shouldOperate() throws Exception {
        UUID campaignId = UUID.fromString("76c7f712-4d99-44b5-b27f-15acd7e1a410");

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .id(campaignId)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        NotificationStatistic notificationStatistic = NotificationStatistic.builder()
                .inStateCreated(1)
                .inStateScheduled(0)
                .inStateQueued(0)
                .inStateAccepted(0)
                .inStateRejected(0)
                .inStateReceived(0)
                .inStateAcknowledged(0)
                .inStateFailed(0)
                .build();

        for (int i = 0; i < 100; i += 1) {
            campaignRepository.save(campaign
                    .setState(CampaignState.RUNNING)
                    .setCreated(0));

            CompletableFuture.allOf(
                    CompletableFuture.runAsync(() ->
                            campaignService.pauseCampaign(campaignId)),
                    CompletableFuture.runAsync(() ->
                            campaignService.updateCampaignStats(campaignId, notificationStatistic)))
                    .join();

            Campaign result = campaignRepository.findById(campaignId)
                    .orElseThrow(AssertionError::new);

            assertThat(result.getState(), is(CampaignState.PAUSE));
            assertThat(result.getCreated(), is(1));
        }
    }
}
