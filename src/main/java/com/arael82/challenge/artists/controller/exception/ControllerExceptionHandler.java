package com.arael82.challenge.artists.controller.exception;

import com.arael82.challenge.artists.service.exception.NotFoundException;
import com.arael82.challenge.artists.service.exception.UnexpectedServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleResourceNotFoundException(NotFoundException ex) {
        return new ErrorResponseDto(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnexpectedServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleUnexpectedServiceException(UnexpectedServiceException ex) {
        return new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleAnyOtherException(Exception ex) {
        return new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
