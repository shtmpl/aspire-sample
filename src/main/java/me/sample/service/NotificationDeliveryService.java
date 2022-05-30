package me.sample.service;

import me.sample.domain.data.NotificationData;
import me.sample.domain.data.NotificationStateData;

import java.util.UUID;

public interface NotificationDeliveryService {

    UUID sendNotification(UUID id);

    NotificationStateData sendNotificationData(NotificationData data);

    void sendNotificationViaQueue(UUID id);
}
