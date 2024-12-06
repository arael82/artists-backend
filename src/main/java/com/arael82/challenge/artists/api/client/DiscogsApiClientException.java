package com.arael82.challenge.artists.api.client;

import java.io.IOException;

public class DiscogsApiClientException extends IOException {
    public DiscogsApiClientException(String message) {
        super(message);
    }
}
