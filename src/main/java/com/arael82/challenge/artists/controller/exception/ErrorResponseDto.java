package com.arael82.challenge.artists.controller.exception;

import org.springframework.http.HttpStatus;

public record ErrorResponseDto(
        HttpStatus Status,
        String message) {}
