package me.sample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import me.sample.domain.Code;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeDTO {
    UUID id;
    Code.CodeType type;
    Code.CodeAccess access;
    Boolean isUsed;
    String value;
}
