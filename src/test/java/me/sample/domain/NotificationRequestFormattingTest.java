package me.sample.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import me.sample.gateway.push.apns.request.ApnsRequest;
import me.sample.gateway.push.apns.request.ApnsRequestAlert;
import me.sample.gateway.push.apns.request.ApnsRequestData;
import me.sample.gateway.push.apns.request.ApnsRequestServiceParams;
import me.sample.gateway.push.fcm.request.FcmRequest;
import me.sample.gateway.push.fcm.request.FcmRequestData;

import java.util.Map;
import java.util.UUID;

public class NotificationRequestFormattingTest {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void shouldFormatApnsRequest() throws Exception {
        UUID campaignId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        String result = MAPPER.writeValueAsString(ApnsRequest.builder()
                .serviceParams(ApnsRequestServiceParams.builder()
                        .alert(ApnsRequestAlert.builder()
                                .title("NotificationTemplate.subject")
                                .body("NotificationTemplate.text")
                                .build())
                        .build())
                .data(ApnsRequestData.builder()
                        .campaignId(campaignId)
                        .notificationId(notificationId)
                        .build())
                .build()
                .addCustomJsonData("NotificationTemplate.customPushPartName", "{ \"value\": 42 }"));

        System.out.println(result);
    }

    @Test
    public void shouldFormatFcmRequest() throws Exception {
        UUID campaignId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        Map<String, Object> result = MAPPER.convertValue(
                FcmRequest.builder()
                        .title("NotificationTemplate.subject")
                        .body("NotificationTemplate.text")
                        .build()
                        .setData(FcmRequestData.builder()
                                .notificationId(notificationId)
                                .build()
                                .addCustomData(
                                        "NotificationTemplate.customPushPartName",
                                        "{ \"value\": 42 }")),
                new TypeReference<Map<String, Object>>() {
                });

        System.out.println(result);
    }
}
