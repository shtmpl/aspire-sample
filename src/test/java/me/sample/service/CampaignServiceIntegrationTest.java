package me.sample.service;

import me.sample.repository.CampaignRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.DistributionRepository;
import me.sample.repository.NotificationRepository;
import me.sample.repository.ScheduledGeoposDisseminationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.NotificationState;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignState;
import me.sample.domain.Company;
import me.sample.domain.BadResourceException;
import me.sample.domain.Notification;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CampaignServiceIntegrationTest {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DistributionRepository distributionRepository;

    @Autowired
    private ScheduledGeoposDisseminationRepository scheduledGeoposDisseminationRepository;

    @Test
    public void shouldSaveCampaign() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Campaign result = campaignService.saveCampaign(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getCreatedDate(), is(greaterThanOrEqualTo(now)));
        assertThat(result.getUpdatedDate(), is(greaterThanOrEqualTo(now)));
    }

    @Test
    public void shouldNotSaveCampaignWithExistingId() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        try {
            campaignService.saveCampaign(Campaign.builder()
                    .id(campaign.getId())
                    .build());

            fail();
        } catch (BadResourceException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }

    @Test
    public void shouldUpdateCampaignStats() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .state(CampaignState.RUNNING)
                .name(String.valueOf(UUID.randomUUID()))
                .build());


        EnumSet.allOf(NotificationState.class).forEach((NotificationState state) ->
                notificationRepository.save(Notification.builder()
                        .campaign(campaign)
                        .state(state)
                        .build()));


        campaignService.updateCampaignsStats();


        Campaign found = campaignRepository.findById(campaign.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getCreated(), is(8));
        assertThat(found.getSent(), is(3));
        assertThat(found.getDelivered(), is(2));
        assertThat(found.getOpened(), is(1));
        assertThat(found.getFail(), is(2));
    }
}
