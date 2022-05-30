package me.sample.web.rest.errors;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnknownAppBundleKeyException extends RuntimeException {
    public UnknownAppBundleKeyException() {
    }

    public UnknownAppBundleKeyException(String message) {
        super(message);
    }
}
