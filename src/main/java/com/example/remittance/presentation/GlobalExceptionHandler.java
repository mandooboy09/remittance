package com.example.remittance.presentation;


import com.example.remittance.presentation.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception ex) {
        log.error("exception occurred: " + ex);
        return ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

    }
}
