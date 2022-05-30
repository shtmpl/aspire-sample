package me.sample.web.rest.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.domain.NotificationState;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NotificationSendResponse {

    NotificationState state;

    String stateReason;

    String gatewayRequest;

    String gatewayResponse;
}
