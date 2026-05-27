package com.example.traitortracing.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    INVALID_KEY(1001, "User existed !", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "User does not exist !", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1003, "Unauthenticated !", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1003, "You do not have permission", HttpStatus.FORBIDDEN),
    IMAGE_NOT_EXISTED(1004, "Image does not exist", HttpStatus.FORBIDDEN),;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
