package me.sample.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PropFilter implements Serializable {
    String name;
    Object value;
    Sign sign;

    public enum Sign {
        EQUAL, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL
    }
}
