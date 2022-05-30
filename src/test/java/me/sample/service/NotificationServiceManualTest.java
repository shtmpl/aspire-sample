package me.sample.service;

import me.sample.repository.CampaignRepository;
import me.sample.repository.NotificationTemplateRepository;
import me.sample.repository.TerminalRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.domain.NotificationTemplate;
import me.sample.domain.Terminal;
import me.sample.domain.TerminalPlatform;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "gateway.proxy.enabled=true"
})
public class NotificationServiceManualTest {

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;


    @Autowired
    private NotificationService notificationService;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldSendNotification() throws Exception {
        Terminal terminal = terminalRepository.save(Terminal.builder()
                .platform(TerminalPlatform.IOS)
                .pushId("965b251c6cb1926de3cb366fdfb16ddde6b9086a8a3cac9e5f857679376eab7c")
                .build());

        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(NotificationTemplate.builder()
                .subject("Subject")
                .text("Text")
                .build());

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .notificationTemplate(notificationTemplate)
                .name(String.valueOf(UUID.randomUUID()))
                .build());


        notificationService.sendNotification(terminal, campaign);


        Thread.sleep(10000);
    }
}
