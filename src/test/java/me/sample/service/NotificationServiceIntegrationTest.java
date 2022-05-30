package me.sample.service;

import me.sample.repository.NotificationRepository;
import me.sample.repository.NotificationStateLogRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.domain.Notification;
import me.sample.domain.NotificationLimitValidationResult;
import me.sample.domain.NotificationState;
import me.sample.domain.NotificationStateLog;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationStateLogRepository notificationStateLogRepository;

    @Autowired
    private NotificationService notificationService;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldValidateNotificationLimit() throws Exception {
        NotificationLimitValidationResult result = notificationService.validateNotificationLimit(Campaign.builder()
                .build());

        assertThat(result.getFailed(), is(false));
        assertThat(result.getLimit(), is(nullValue()));
        assertThat(result.getCount(), is(0L));
    }

    @Test
    public void shouldSaveNotification() throws Exception {
        Notification notification = notificationService.saveNotification(Notification.builder()
                .state(NotificationState.CREATED)
                .build());


        Notification result = notificationRepository.findById(notification.getId())
                .orElseThrow(AssertionError::new);

        assertThat(result.getState(), is(NotificationState.CREATED));


        List<NotificationStateLog> resultStateLogs = notificationStateLogRepository.findAllByNotificationIdOrderByCreatedAtAsc(notification.getId());

        assertThat(resultStateLogs.size(), is(1));

        assertThat(resultStateLogs.get(0).getState(), is(NotificationState.CREATED));
        assertThat(resultStateLogs.get(0).getStateReason(), is(nullValue()));
    }

    @Test
    public void shouldUpdateNotificationState() throws Exception {
        Notification notification = notificationRepository.save(Notification.builder()
                .state(NotificationState.CREATED)
                .build());


        notificationService.updateNotificationState(notification.getId(), NotificationState.SCHEDULED);


        Notification result = notificationRepository.findById(notification.getId())
                .orElseThrow(AssertionError::new);

        assertThat(result.getState(), is(NotificationState.SCHEDULED));


        List<NotificationStateLog> resultStateLogs = notificationStateLogRepository.findAllByNotificationIdOrderByCreatedAtAsc(notification.getId());

        assertThat(resultStateLogs.size(), is(1));

        assertThat(resultStateLogs.get(0).getState(), is(NotificationState.SCHEDULED));
        assertThat(resultStateLogs.get(0).getStateReason(), is(nullValue()));
    }
}
