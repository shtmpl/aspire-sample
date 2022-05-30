package me.sample.exception;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WrongExressionTypeException extends RuntimeException {
    public WrongExressionTypeException(String message) {
        super(message);
    }

    public WrongExressionTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
