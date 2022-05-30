package me.sample.service;

import me.sample.repository.NotificationTemplateRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.BadResourceException;
import me.sample.domain.NotificationTemplate;

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
public class NotificationTemplateServiceIntegrationTest {

    @Autowired
    private NotificationTemplateService notificationTemplateService;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Before
    public void setUp() throws Exception {
        notificationTemplateRepository.deleteAll();
    }

    @Test
    public void shouldSaveNotificationTemplate() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        NotificationTemplate result = notificationTemplateService.saveNotificationTemplate(NotificationTemplate.builder()
                .build());

        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getCreatedDate(), is(greaterThanOrEqualTo(now)));
        assertThat(result.getUpdatedDate(), is(greaterThanOrEqualTo(now)));
    }

    @Test
    public void shouldNotSaveNotificationTemplateWithExistingId() throws Exception {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(NotificationTemplate.builder()
                .build());

        try {
            notificationTemplateService.saveNotificationTemplate(NotificationTemplate.builder()
                    .id(notificationTemplate.getId())
                    .build());

            fail();
        } catch (BadResourceException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }

    @Test
    public void shouldUpdateNotificationTemplate() throws Exception {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(NotificationTemplate.builder()
                .build());

        NotificationTemplate result = notificationTemplateService.updateNotificationTemplate(notificationTemplate.getId(), NotificationTemplate.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build())
                .orElseThrow(AssertionError::new);

        assertThat(result.getId(), is(notificationTemplate.getId()));
        assertThat(result.getCreatedDate(), is(notificationTemplate.getCreatedDate()));
        assertThat(result.getUpdatedDate(), is(greaterThan(notificationTemplate.getUpdatedDate())));
    }
}
