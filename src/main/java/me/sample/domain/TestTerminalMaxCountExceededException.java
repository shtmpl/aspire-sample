package me.sample.domain;

public class TestTerminalMaxCountExceededException extends RuntimeException {

    public TestTerminalMaxCountExceededException() {
        super();
    }

    public TestTerminalMaxCountExceededException(String message) {
        super(message);
    }

    public TestTerminalMaxCountExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestTerminalMaxCountExceededException(Throwable cause) {
        super(cause);
    }
}
