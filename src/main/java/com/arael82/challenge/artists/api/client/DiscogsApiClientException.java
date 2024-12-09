package com.arael82.challenge.artists.api.client;

import org.springframework.http.HttpStatus;

import java.io.IOException;

public class DiscogsApiClientException extends IOException {

    public final HttpStatus code;

    public DiscogsApiClientException(String message, Integer code) {
        super(message);
        this.code = HttpStatus.resolve(code);
    }
}
