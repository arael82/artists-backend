package com.arael82.challenge.artists.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UnexpectedServiceException extends RuntimeException {

    public UnexpectedServiceException(Throwable cause) {
        super(cause);
    }
}
