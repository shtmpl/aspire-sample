package me.sample.exception;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ExceptionTranslator {

    private final MessageSource messageSource;

    public ExceptionTranslator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(InvalidUUIDException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ApiError handleInvalidUUIDException(InvalidUUIDException e) {
        return new ApiError(e.getMessage());
    }

    @ExceptionHandler
    protected ResponseEntity<?> handleBindException(BindException exception) {
        return ResponseEntity.badRequest().body(convert(exception.getAllErrors()));
    }

    /**
     * Exception handler for validation errors caused by method parameters @RequesParam, @PathVariable, @RequestHeader annotated with javax.validation constraints.
     */
    @ExceptionHandler
    protected ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException exception) {

        List<ApiError> apiErrors = new ArrayList<>();

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String value = (violation.getInvalidValue() == null ? null : violation.getInvalidValue().toString());
            apiErrors.add(new ApiError(violation.getPropertyPath().toString(), value, violation.getMessage()));
        }

        return ResponseEntity.badRequest().body(apiErrors);
    }

    /**
     * Exception handler for @RequestBody validation errors.
     */
    @ExceptionHandler
    protected ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        return ResponseEntity.badRequest().body(convert(exception.getBindingResult().getAllErrors()));
    }

    /**
     * Exception handler for missing required parameters errors.
     */
    @ExceptionHandler
    protected ResponseEntity<?> handleServletRequestBindingException(ServletRequestBindingException exception) {

        return ResponseEntity.badRequest().body(new ApiError(null, null, exception.getMessage()));
    }

    /**
     * Exception handler for invalid payload (e.g. json invalid format exception).
     */
    @ExceptionHandler
    protected ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {

        return ResponseEntity.badRequest().body(new ApiError(null, null, exception.getMessage()));
    }

    private List<ApiError> convert(List<ObjectError> objectErrors) {

        List<ApiError> apiErrors = new ArrayList<>();

        for (ObjectError objectError : objectErrors) {

            String message = objectError.getDefaultMessage();
            if (message == null) {
                //when using custom spring validator org.springframework.validation.Validator need to resolve messages manually
                message = messageSource.getMessage(objectError, null);
            }

            ApiError apiError = null;
            if (objectError instanceof FieldError) {
                FieldError fieldError = (FieldError) objectError;
                String value = (fieldError.getRejectedValue() == null ? null : fieldError.getRejectedValue().toString());
                apiError = new ApiError(fieldError.getField(), value, message);
            } else {
                apiError = new ApiError(objectError.getObjectName(), objectError.getCode(), objectError.getDefaultMessage());
            }

            apiErrors.add(apiError);
        }

        return apiErrors;
    }

}
