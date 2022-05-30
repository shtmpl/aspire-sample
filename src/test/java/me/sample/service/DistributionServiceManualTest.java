package me.sample.service;

import me.sample.repository.NotificationTemplateRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.domain.Distribution;
import me.sample.domain.NotificationLimitValidationResult;
import me.sample.domain.NotificationTemplate;
import me.sample.domain.Terminal;

import java.time.LocalDateTime;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributionServiceManualTest {

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldOperate() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(NotificationTemplate.builder()
                .build());

        Campaign campaign = Campaign.builder()
                .notificationTemplate(notificationTemplate)
                .name(String.valueOf(UUID.randomUUID()))
                .build();
        Distribution dissemination = Distribution.builder()
                .campaign(campaign)
                .start(now.toLocalDate())
                .end(now.plusDays(1).toLocalDate())
                .cron("* * * * * ?")
                .build();
        campaign.setDistribution(dissemination);


        Mockito.doAnswer((InvocationOnMock invocation) -> NotificationLimitValidationResult.builder()
                .failed(false)
                .limit(null)
                .count(0L)
                .build()).when(notificationService).validateNotificationLimit(Mockito.any(Campaign.class));

        Mockito.doAnswer((InvocationOnMock invocation) -> NotificationLimitValidationResult.builder()
                .failed(false)
                .limit(null)
                .count(0L)
                .build()).when(notificationService).validateNotificationLimit(Mockito.any(Campaign.class), Mockito.any(Terminal.class));

        Mockito.doAnswer((InvocationOnMock invocation) -> NotificationLimitValidationResult.builder()
                .failed(false)
                .limit(null)
                .count(0L)
                .build()).when(notificationService).validateNotificationLimit(Mockito.any(Terminal.class));


        dissemination = distributionService.saveDissemination(dissemination);

        dissemination = distributionService.startDissemination(dissemination);

        Thread.sleep(120000);

//        dissemination = distributionService.pauseDissemination(dissemination);

//        distributionService.deleteDissemination(dissemination.getId());
    }
}
