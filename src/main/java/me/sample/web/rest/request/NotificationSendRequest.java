package me.sample.web.rest.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.domain.TerminalPlatform;

import javax.validation.constraints.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NotificationSendRequest {

    @NotNull
    TerminalPlatform terminalPlatform;

    @NotNull
    String terminalPushId;

    String subject;

    String body;
}
