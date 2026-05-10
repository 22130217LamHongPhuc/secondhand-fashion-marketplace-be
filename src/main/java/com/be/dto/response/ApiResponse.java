package com.be.dto.response;

public record ApiResponse<T>(
        T data,
        String message
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }
}
