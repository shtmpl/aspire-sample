package me.sample.gateway.push.fcm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.gateway.GatewayException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import me.sample.gateway.push.fcm.request.FcmRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class FcmGateway {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }


    FirebaseMessaging firebaseMessaging;

    @Async
    public CompletableFuture<String> sendRequestAsync(String token, FcmRequest request) {
        Map<String, String> data = MAPPER.convertValue(request, new TypeReference<Map<String, String>>() {
        });

        Message message = Message.builder()
                .setToken(token)
                .putAllData(data)
                .build();

        log.debug("Sending message: {}...", message);

        try {
            return CompletableFuture.completedFuture(firebaseMessaging.send(message));
        } catch (FirebaseMessagingException exception) {
            throw new GatewayException(exception);
        }
    }
}
