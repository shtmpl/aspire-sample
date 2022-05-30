package me.sample.exception;


public class InvalidUUIDException extends RuntimeException {

    public InvalidUUIDException(String uuid) {
        super("Invalid UUID string: " + uuid);
    }
}
