package me.sample.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import me.sample.domain.NotificationState;

import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class NotificationDTO {

    UUID id;

    LocalDateTime createdDate;

    LocalDateTime updatedDate;

    NotificationState state;

    String text;
}
