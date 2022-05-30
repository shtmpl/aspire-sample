package me.sample.web.rest.errors;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorityException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "User doesn't have the authority to do the operation";

    public AuthorityException(String message) {
        super(message);
    }

    public AuthorityException() {
        super(DEFAULT_MESSAGE);
    }
}
