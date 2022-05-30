package me.sample.service.impl;

import com.eatthepath.pushy.apns.ApnsPushNotification;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import me.sample.config.RabbitConfiguration;
import me.sample.gateway.push.apns.ApnsGateway;
import me.sample.gateway.push.apns.request.ApnsRequest;
import me.sample.gateway.push.apns.request.ApnsRequestAlert;
import me.sample.gateway.push.apns.request.ApnsRequestData;
import me.sample.gateway.push.apns.request.ApnsRequestServiceParams;
import me.sample.gateway.push.fcm.FcmGateway;
import me.sample.gateway.push.fcm.request.FcmRequest;
import me.sample.gateway.push.fcm.request.FcmRequestData;
import me.sample.domain.Campaign;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Notification;
import me.sample.domain.NotificationState;
import me.sample.domain.NotificationTemplate;
import me.sample.domain.Terminal;
import me.sample.domain.TerminalPlatform;
import me.sample.domain.data.NotificationData;
import me.sample.domain.data.NotificationStateData;
import me.sample.domain.event.NotificationSendRequestEvent;
import me.sample.domain.event.NotificationSendResponseEvent;
import me.sample.repository.NotificationRepository;
import me.sample.service.NotificationDeliveryService;
import me.sample.service.NotificationService;
import me.sample.utils.JsonUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class NotificationDeliveryServiceImpl implements NotificationDeliveryService {

    FcmGateway fcmGateway;
    ApnsGateway apnsGateway;

//    RabbitTemplate rabbitTemplate;

    NotificationRepository notificationRepository;

    @NonFinal
    NotificationService notificationService;

    @Autowired
    public void setNotificationService(@Lazy NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public UUID sendNotification(UUID id) {
        log.debug(".sendNotification(id: {})", id);

        Notification found = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundResourceException("Notification", id));

        Terminal terminal = found.getTerminal();
        UUID terminalId = terminal.getId();

        TerminalPlatform terminalPlatform = terminal.getPlatform();
        if (terminalPlatform == null) {
            log.warn("Notification skipped for terminal id: {}. Reason: No terminal platform provided",
                    terminalId);

            return id;
        }

        String terminalPushId = terminal.getPushId();
        if (terminalPushId == null || terminalPushId.trim().isEmpty()) {
            log.warn("Notification skipped for terminal id: {}. Reason: No terminal push id provided",
                    terminalId);

            return id;
        }

        Campaign campaign = found.getCampaign();
        NotificationTemplate notificationTemplate = campaign.getNotificationTemplate();
        switch (terminalPlatform) {
            case ANDROID:
                sendFcmNotificationAsync(
                        terminalPushId,
                        notificationTemplate.getSubject(),
                        notificationTemplate.getText(),
                        AuxiliaryNotificationData.builder()
                                .campaignId(campaign.getId())
                                .notificationId(id)
                                .customDataKey(notificationTemplate.getCustomPushPartName())
                                .customDataValue(notificationTemplate.getCustomPushPartValue())
                                .build())
                        .thenAccept((NotificationStateData data) ->
                                notificationService.updateNotificationState(
                                        id,
                                        data.getState(),
                                        data.getStateReason()));

                break;
            case IOS:
                sendApnsNotificationAsync(
                        terminalPushId,
                        notificationTemplate.getSubject(),
                        notificationTemplate.getText(),
                        AuxiliaryNotificationData.builder()
                                .campaignId(campaign.getId())
                                .notificationId(id)
                                .customDataKey(notificationTemplate.getCustomPushPartName())
                                .customDataValue(notificationTemplate.getCustomPushPartValue())
                                .build())
                        .thenAccept((NotificationStateData data) ->
                                notificationService.updateNotificationState(
                                        id,
                                        data.getState(),
                                        data.getStateReason()));

                break;
            default:
                log.warn("Notification skipped for terminal id: {}. Reason: Unsupported terminal platform: {}",
                        terminalId,
                        terminalPlatform);

                break;
        }

        return id;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public NotificationStateData sendNotificationData(NotificationData data) {
        TerminalPlatform terminalPlatform = data.getTerminalPlatform();
        if (terminalPlatform == null) {
            throw new UnsupportedOperationException("No terminal platform provided");
        }

        String terminalPushId = data.getTerminalPushId();

        NotificationStateData result;
        switch (terminalPlatform) {
            case ANDROID:
                result = sendFcmNotificationAsync(
                        terminalPushId,
                        data.getSubject(),
                        data.getBody(),
                        AuxiliaryNotificationData.builder()
                                .build())
                        .join();

                break;
            case IOS:
                result = sendApnsNotificationAsync(
                        terminalPushId,
                        data.getSubject(),
                        data.getBody(),
                        AuxiliaryNotificationData.builder()
                                .build())
                        .join();

                break;
            default:
                throw new UnsupportedOperationException(String.format(
                        "Unsupported terminal platform provided: %s",
                        terminalPlatform));
        }

        return result;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void sendNotificationViaQueue(UUID id) {
        log.debug(".sendNotificationViaQueue(id: {})", id);

//        rabbitTemplate.convertAndSend(
//                RabbitConfiguration.QUEUE_NAME_NOTIFICATION_SEND_REQUEST,
//                NotificationSendRequestEvent.builder()
//                        .id(id)
//                        .build());
    }

    @RabbitListener(queues = RabbitConfiguration.QUEUE_NAME_NOTIFICATION_SEND_REQUEST)
    public void onNotificationSendRequest(NotificationSendRequestEvent request) {
        UUID id = request.getId();
        log.debug(".onNotificationSendRequest(id: {})", id);

        Notification found = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundResourceException("Notification", id));

        Terminal terminal = found.getTerminal();
        UUID terminalId = terminal.getId();
        TerminalPlatform terminalPlatform = terminal.getPlatform();
        if (terminalPlatform == null) {
            log.warn("Notification skipped for terminal id: {}. Reason: No terminal platform provided",
                    terminalId);

            return;
        }

        String terminalPushId = terminal.getPushId();
        if (terminalPushId == null || terminalPushId.trim().isEmpty()) {
            log.warn("Notification skipped for terminal id: {}. Reason: No terminal push id provided",
                    terminalId);

            return;
        }

        Campaign campaign = found.getCampaign();
        NotificationTemplate notificationTemplate = campaign.getNotificationTemplate();
//        switch (terminalPlatform) {
//            case ANDROID:
//                sendFcmNotificationAsync(
//                        terminalPushId,
//                        notificationTemplate.getSubject(),
//                        notificationTemplate.getText(),
//                        AuxiliaryNotificationData.builder()
//                                .campaignId(campaign.getId())
//                                .notificationId(id)
//                                .customDataKey(notificationTemplate.getCustomPushPartName())
//                                .customDataValue(notificationTemplate.getCustomPushPartValue())
//                                .build())
//                        .thenAccept((NotificationStateData data) ->
//                                rabbitTemplate.convertAndSend(
//                                        RabbitConfiguration.QUEUE_NAME_NOTIFICATION_SEND_RESPONSE,
//                                        NotificationSendResponseEvent.builder()
//                                                .id(id)
//                                                .state(data.getState())
//                                                .stateReason(data.getStateReason())
//                                                .build()));
//
//                break;
//            case IOS:
//                sendApnsNotificationAsync(
//                        terminalPushId,
//                        notificationTemplate.getSubject(),
//                        notificationTemplate.getText(),
//                        AuxiliaryNotificationData.builder()
//                                .campaignId(campaign.getId())
//                                .notificationId(id)
//                                .customDataKey(notificationTemplate.getCustomPushPartName())
//                                .customDataValue(notificationTemplate.getCustomPushPartValue())
//                                .build())
//                        .thenAccept((NotificationStateData data) ->
//                                rabbitTemplate.convertAndSend(
//                                        RabbitConfiguration.QUEUE_NAME_NOTIFICATION_SEND_RESPONSE,
//                                        NotificationSendResponseEvent.builder()
//                                                .id(id)
//                                                .state(data.getState())
//                                                .stateReason(data.getStateReason())
//                                                .build()));
//
//                break;
//            default:
//                log.warn("Notification skipped for terminal id: {}. Reason: Unsupported terminal platform: {}",
//                        terminalId,
//                        terminalPlatform);
//
//                break;
//        }
    }

    @RabbitListener(queues = RabbitConfiguration.QUEUE_NAME_NOTIFICATION_SEND_RESPONSE)
    public void onNotificationSendResponse(NotificationSendResponseEvent response) {
        UUID id = response.getId();
        NotificationState state = response.getState();
        String stateReason = response.getStateReason();
        log.debug(".onNotificationSendResponse(id: {}, state: {}, stateReason: {})",
                id, state, stateReason);

        notificationService.updateNotificationState(id, state, stateReason);
    }

    private CompletableFuture<NotificationStateData> sendFcmNotificationAsync(String terminalPushId,
                                                                              String subject, String body,
                                                                              AuxiliaryNotificationData data) {
        UUID notificationId = data.getNotificationId();

        FcmRequest request = FcmRequest.builder()
                .title(subject)
                .body(body)
                .build()
                .setData(FcmRequestData.builder()
                        .notificationId(notificationId)
                        .build()
                        .addCustomData(
                                data.getCustomDataKey(),
                                data.getCustomDataValue()));

        return fcmGateway.sendRequestAsync(terminalPushId, request)
                .handle((String response, Throwable throwable) -> {
                    if (response != null) {
                        log.info("Notification id: {} accepted by server: response {}",
                                notificationId,
                                response);

                        return NotificationStateData.builder()
                                .state(NotificationState.ACCEPTED_BY_SERVER)
                                .gatewayRequest(JsonUtil.json(request))
                                .gatewayResponse(response)
                                .build();
                    } else {
                        log.error("Notification id: {} failed: reason: {}",
                                notificationId,
                                throwable);

                        return NotificationStateData.builder()
                                .state(NotificationState.FAILED)
                                .stateReason(formatFailureReason(throwable))
                                .gatewayRequest(JsonUtil.json(request))
                                .gatewayResponse(response)
                                .build();
                    }
                });
    }

    private CompletableFuture<NotificationStateData> sendApnsNotificationAsync(String terminalPushId,
                                                                               String subject, String body,
                                                                               AuxiliaryNotificationData data) {
        UUID notificationId = data.getNotificationId();

        ApnsRequest request = ApnsRequest.builder()
                .serviceParams(ApnsRequestServiceParams.builder()
                        .alert(ApnsRequestAlert.builder()
                                .title(subject)
                                .body(body)
                                .build())
                        .build())
                .data(ApnsRequestData.builder()
                        .campaignId(data.getCampaignId())
                        .notificationId(notificationId)
                        .build())
                .build()
                .addCustomJsonData(
                        data.getCustomDataKey(),
                        data.getCustomDataValue());

        return apnsGateway.sendRequestAsync(terminalPushId, request)
                .handle((PushNotificationResponse<? extends ApnsPushNotification> response, Throwable throwable) -> {
                    if (response != null) {
                        if (response.isAccepted()) {
                            log.info("Notification id: {} accepted by server: response {}",
                                    notificationId,
                                    response);

                            return NotificationStateData.builder()
                                    .state(NotificationState.ACCEPTED_BY_SERVER)
                                    .gatewayRequest(JsonUtil.json(request))
                                    .gatewayResponse(String.valueOf(response))
                                    .build();
                        } else {
                            String rejectionReason = response.getRejectionReason();
                            log.error("Notification id: {} rejected by server: response {}, reason: {}",
                                    notificationId,
                                    response,
                                    rejectionReason);

                            return NotificationStateData.builder()
                                    .state(NotificationState.REJECTED_BY_SERVER)
                                    .stateReason(rejectionReason)
                                    .gatewayRequest(JsonUtil.json(request))
                                    .gatewayResponse(String.valueOf(response))
                                    .build();
                        }
                    } else {
                        log.error("Notification id: {} failed: response: {}, reason: {}",
                                notificationId,
                                response,
                                throwable);

                        return NotificationStateData.builder()
                                .state(NotificationState.FAILED)
                                .stateReason(formatFailureReason(throwable))
                                .gatewayRequest(JsonUtil.json(request))
                                .gatewayResponse(String.valueOf(response))
                                .build();
                    }
                });
    }

    private static String formatFailureReason(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        return String.format("%s: %s",
                throwable.getClass().getCanonicalName(),
                throwable.getMessage());
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class AuxiliaryNotificationData {

        UUID campaignId;

        UUID notificationId;

        String customDataKey;

        String customDataValue;
    }
}
