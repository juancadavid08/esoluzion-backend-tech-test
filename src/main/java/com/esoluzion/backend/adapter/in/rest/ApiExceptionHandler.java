package com.esoluzion.backend.adapter.in.rest;

import com.esoluzion.backend.domain.exception.ProductNotFoundException;
import com.esoluzion.backend.domain.exception.UpstreamServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ProductNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleInvalidParameter(ConstraintViolationException exception,
                                                            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", exception.getMessage(), request);
    }

    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<ApiError> handleUpstream(UpstreamServiceException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_GATEWAY, "UPSTREAM_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", request);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                               HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
