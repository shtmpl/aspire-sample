package me.sample.gateway.push.apns.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ApnsRequestServiceParams {

    ApnsRequestAlert alert;

    @Builder.Default
    String sound = "default";

    @Builder.Default
    @JsonProperty("mutable-content")
    Long mutableContent = 1L;

    @JsonProperty("content-available")
    Integer contentAvailable;
}
