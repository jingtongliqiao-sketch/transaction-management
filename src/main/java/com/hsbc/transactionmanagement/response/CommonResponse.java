package com.hsbc.transactionmanagement.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {
    private Status status;
    private T result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private int code;
        private String message;
    }

    // success without data
    public static CommonResponse<Void> success() {
        return new CommonResponse<>(new Status(200, "Success"), null);
    }

    // success with data
    public static <T> CommonResponse<Void> success(String message) {
        return new CommonResponse<>(new Status(200, message), null);
    }

    // success with custom message
    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(new Status(200, message), data);
    }

    // error response with custom code and message
    public static <T> CommonResponse<T> error(int code, String message) {
        return new CommonResponse<>(new Status(code, message), null);
    }

    // 400 Bad Request
    public static <T> CommonResponse<T> badRequest(String message) {
        return error(400, message);
    }

    // 404 Not Found
    public static <T> CommonResponse<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> CommonResponse<T> conflict(String message) {
        return error(409, message);
    }

    public static <T> CommonResponse<T> internalError(String message) {
        return error(500, message);
    }
}