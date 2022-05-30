package me.sample.gateway.push.apns.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.utils.JsonUtil;

import java.util.LinkedHashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ApnsRequest {

    @JsonProperty("aps")
    ApnsRequestServiceParams serviceParams;

    @JsonProperty("data")
    ApnsRequestData data;

    @Builder.Default
    Map<String, Object> customData = new LinkedHashMap<>();

    public ApnsRequest addCustomJsonData(String key, String json) {
        if (key != null) {
            customData.put(key, JsonUtil.unmarshallMapFromJson(json));
        }

        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getCustomData() {
        return customData;
    }
}
