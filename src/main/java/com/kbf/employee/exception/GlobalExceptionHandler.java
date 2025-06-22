package com.kbf.employee.exception;

import com.kbf.employee.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(
                ErrorResponse.create(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed: " + errors,
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.create(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.create(
                        HttpStatus.CONFLICT,
                        ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorage(
            FileStorageException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.create(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(
            InvalidFileException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                ErrorResponse.create(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.create(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(
            InvalidOperationException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.create(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.create(
                        HttpStatus.FORBIDDEN,
                        "Access denied: " + ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.create(
                        HttpStatus.UNAUTHORIZED,
                        ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.create(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred: " + ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.create(
                        HttpStatus.CONFLICT,
                        "Database constraint violation: " + ex.getRootCause().getMessage(),
                        request.getDescription(false)
                )
        );
    }
}