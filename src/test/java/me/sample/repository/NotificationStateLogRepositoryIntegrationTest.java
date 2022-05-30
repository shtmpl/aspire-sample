package me.sample.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Notification;
import me.sample.domain.NotificationStateLog;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationStateLogRepositoryIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationStateLogRepository notificationStateLogRepository;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldSaveNotificationStateLog() throws Exception {
        Notification notification = notificationRepository.save(Notification.builder()
                .build());


        NotificationStateLog result = notificationStateLogRepository.save(NotificationStateLog.builder()
                .notification(notification)
                .build());


        assertThat(result.getId(), is(notNullValue()));
    }
}
