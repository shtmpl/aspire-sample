package me.sample.gateway.push.fcm.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.utils.JsonUtil;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FcmRequest {

    String title;

    String body;

    @JsonProperty("data")
    String data;

    public FcmRequest setData(FcmRequestData data) {
        this.data = JsonUtil.json(data);

        return this;
    }
}
