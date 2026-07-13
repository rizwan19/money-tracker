package com.rizwan.money_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class RefreshTokenStoreUnavailableException extends RuntimeException {
    public RefreshTokenStoreUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
