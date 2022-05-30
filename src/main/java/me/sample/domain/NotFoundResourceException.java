package me.sample.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundResourceException extends RuntimeException {

    public NotFoundResourceException() {
        super();
    }

    public NotFoundResourceException(String message) {
        super(message);
    }

    public NotFoundResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundResourceException(Throwable cause) {
        super(cause);
    }

    public NotFoundResourceException(String entityName, Object identity) {
        this(String.format("%s[%s] is not found", entityName, identity));
    }
}
