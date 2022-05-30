package me.sample.gateway.push;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import liquibase.util.StringUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.utils.JsonUtil;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class AppleNotification implements Serializable {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Getter
    @JsonIgnore
    UUID id;

    @JsonIgnore
    Map<String, Object> data;

    @JsonIgnore
    String customDataName;

    @JsonIgnore
    String customDataValue;

    @Getter
    Alert alert;

    @Getter
    String sound;

    @JsonProperty("mutable-content")
    Long mutableContent = 1L;

    @JsonProperty("content-available")
    Integer contentAvailable;

    @Builder
    public AppleNotification(UUID id,
                             String deviceToken,
                             PushNotification notification,
                             String title,
                             String customDataName,
                             String customDataValue,
                             LocalDateTime createTime) {
        this.data = JsonUtil.marshallToMap(notification);
        this.data.remove("text");

        Number paymentId = (Number) data.get("payment_id");
        if (paymentId != null) {
            data.put("payment_id", Long.toString(paymentId.longValue()));
        }

        this.id = id;
        this.data.put("id", id.toString());
        this.alert = new Alert(title, notification.getText());

        this.sound = "default";

        this.contentAvailable = null;

        if (StringUtils.isNotEmpty(customDataName)) {
            this.customDataName = customDataName;
            this.customDataValue = customDataValue;
        } else {
            this.customDataName = null;
            this.customDataValue = null;
        }
    }

    @SneakyThrows(IOException.class)
    public String payload() {
        ImmutableMap<String, Object> params = Optional.ofNullable(customDataName)
                .map(cdm -> ImmutableMap.of(
                        "aps", this,
                        "data", data,
                        customDataName, JsonUtil.unmarshallMapFromJson(customDataValue)))
                .orElseGet(() -> ImmutableMap.of(
                        "aps", this,
                        "data", ImmutableMap.of("id", id.toString())));
        return OBJECT_MAPPER.writeValueAsString(params);
    }


    private void fixData() {
        data.remove("text");
        Number paymentId = (Number) data.get("payment_id");
        if (paymentId != null) {
            data.put("payment_id", Long.toString(paymentId.longValue()));
        }
    }

}
