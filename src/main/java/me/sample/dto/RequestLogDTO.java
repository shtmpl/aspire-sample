package me.sample.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestLogDTO {
    UUID id;
    LocalDateTime date;
    String request;
    Object data;
    UUID terminalId;
}
