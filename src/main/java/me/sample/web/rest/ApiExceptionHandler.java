package me.sample.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.BadResourceException;
import me.sample.domain.TestTerminalMaxCountExceededException;
import me.sample.web.rest.errors.AuthorityException;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.response.ResponseError;

@ControllerAdvice
public class ApiExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createResponseError(exception.getMessage()));
    }

    @ExceptionHandler(TestTerminalMaxCountExceededException.class)
    public ResponseEntity<?> handleTerminalLimitExceededException(TestTerminalMaxCountExceededException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createResponseError(exception.getMessage()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<?> handleUnsupportedOperationException(UnsupportedOperationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createResponseError(exception.getMessage()));
    }

    @ExceptionHandler(BadResourceException.class)
    public ResponseEntity<?> handleBadResourceDataException(BadResourceException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createResponseError(exception.getMessage()));
    }

    @ExceptionHandler(BadRequestAlertException.class)
    public ResponseEntity<?> handleBadRequestAlertException(BadRequestAlertException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createResponseError(exception.getMessage()));
    }

    @ExceptionHandler(AuthorityException.class)
    public ResponseEntity<?> handleAuthorityException(AuthorityException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createResponseError(exception.getMessage()));
    }

    @ExceptionHandler(NotFoundResourceException.class)
    public ResponseEntity<?> handleResourceNotFoundException(NotFoundResourceException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createResponseError(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpectedException(Exception exception) {
        logger.error("Unexpected error occurred", exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

    private static ResponseError createResponseError(String message) {
        ResponseError result = new ResponseError();
        result.setMessage(message);

        return result;
    }
}
