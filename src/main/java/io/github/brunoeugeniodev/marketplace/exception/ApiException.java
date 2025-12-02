package io.github.brunoeugeniodev.marketplace.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}

@Getter
class AuthException extends ApiException {
    public AuthException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

@Getter
class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}