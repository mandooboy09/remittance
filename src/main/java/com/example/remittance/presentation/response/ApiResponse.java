package com.example.remittance.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder(access = AccessLevel.PRIVATE)
public record ApiResponse<T>(
        int statusCode,
        String statusMessage,
        String message,
        T data
) {
    public static <T> ApiResponse<T> of(HttpStatus httpStatus, String message, T data) {
        return ApiResponse.<T>builder()
                .statusCode(httpStatus.value())
                .statusMessage(httpStatus.getReasonPhrase())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, String message) {
        return ApiResponse.<T>builder()
                .statusCode(httpStatus.value())
                .statusMessage(httpStatus.getReasonPhrase())
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, T data) {
        return of(httpStatus, httpStatus.getReasonPhrase(), data);
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus) {
        return ApiResponse.<T>builder()
                .statusCode(httpStatus.value())
                .statusMessage(httpStatus.getReasonPhrase())
                .build();
    }
}
