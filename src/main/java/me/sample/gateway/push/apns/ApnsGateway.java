package me.sample.gateway.push.apns;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsPushNotification;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.sample.gateway.push.apns.request.ApnsRequest;
import me.sample.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class ApnsGateway {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }


    ApnsClient apnsClient;

    @NonFinal
    @Value("${push.notification.JWT_TOPIC}")
    String apnsTopic;

    public CompletableFuture<? extends PushNotificationResponse<? extends ApnsPushNotification>> sendRequestAsync(String token, ApnsRequest request) {
        String payload = JsonUtil.json(request);

        log.debug("Sending message: {}...", payload);

        SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(
                token,
                apnsTopic,
                payload);

        return apnsClient.sendNotification(pushNotification);
    }
}
