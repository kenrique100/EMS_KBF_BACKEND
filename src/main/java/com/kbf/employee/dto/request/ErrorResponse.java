package com.kbf.employee.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(LocalDateTime timestamp, int status,
                         String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public static ErrorResponse create(HttpStatus status,
                                       String message,
                                       String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }

    public static ErrorResponse unauthorized(String message) {
        return create(HttpStatus.UNAUTHORIZED, message, null);
    }

    public static ErrorResponse notFound(String message) {
        return create(HttpStatus.NOT_FOUND, message, null);
    }

    public static ErrorResponse internalError(String message) {
        return create(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }
}