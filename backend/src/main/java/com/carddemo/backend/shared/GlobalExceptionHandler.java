package com.carddemo.backend.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> api(ApiException exception) {
        return response(exception.getStatus(), exception.getCode(), exception.getMessage(), exception.getField());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ErrorResponse> optimisticLock() {
        return response(HttpStatus.CONFLICT, "VERSION_CONFLICT", "The user was changed by another request.", "version");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<ErrorResponse> forbidden() {
        return response(HttpStatus.FORBIDDEN, "FORBIDDEN", "Administrator access is required.", null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MethodArgumentNotValidException.class})
    ResponseEntity<ErrorResponse> malformedRequest(Exception exception) {
        String field = exception instanceof MethodArgumentTypeMismatchException mismatch ? mismatch.getName()
                : exception instanceof MissingServletRequestParameterException missing ? missing.getParameterName() : null;
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_REQUIRED", "Request contains an invalid or missing value.", field);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException exception) {
        log.error("Persistence constraint violation traceId={}", traceId(), exception);
        ConstraintViolationException violation = findConstraintViolation(exception);
        if (violation != null && "users_pkey".equalsIgnoreCase(violation.getConstraintName())) {
            return response(HttpStatus.CONFLICT, "DUPLICATE_USER", "User ID already exists.", "userId");
        }
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "A persistence error occurred.", null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected API error method={} uri={} traceId={}", request.getMethod(), request.getRequestURI(), traceId(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.", null);
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String code, String message, String field) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, field, traceId()));
    }

    private String traceId() {
        return MDC.get(TraceIdFilter.MDC_KEY);
    }

    private ConstraintViolationException findConstraintViolation(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException violation) return violation;
            current = current.getCause();
        }
        return null;
    }
}
