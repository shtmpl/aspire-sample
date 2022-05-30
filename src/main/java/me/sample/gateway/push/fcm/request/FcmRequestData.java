package me.sample.gateway.push.fcm.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FcmRequestData {

    @JsonProperty("id")
    UUID notificationId;

    @Builder.Default
    Map<String, Object> customData = new LinkedHashMap<>();

    public FcmRequestData addCustomData(String key, Object value) {
        if (key != null) {
            customData.put(key, value);
        }

        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getCustomData() {
        return customData;
    }
}
