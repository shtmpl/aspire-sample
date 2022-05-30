package me.sample.domain;

/**
 * Общий класс ошибок, связанных с некорректно сформированными сущностями
 */
public class BadResourceException extends RuntimeException {

    public BadResourceException() {
        super();
    }

    public BadResourceException(String message) {
        super(message);
    }

    public BadResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadResourceException(Throwable cause) {
        super(cause);
    }
}
