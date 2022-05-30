package me.sample.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@NoArgsConstructor
@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
public class UnknownSearchCriteria extends RuntimeException {

    public UnknownSearchCriteria(String message) {
        super(message);
    }
}
