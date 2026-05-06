package com.spring.project.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            NotFoundException exception, HttpServletRequest request, Locale locale) {
        log.warn("Not found: {} path={}", exception.getMessage(), request.getRequestURI());
        return buildError(
                HttpStatus.NOT_FOUND,
                messageSource.getMessage("error.not_found", null, "Not found", locale),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyExists(
            AlreadyExistException exception, HttpServletRequest request, Locale locale) {
        log.warn("Conflict: {} path={}", exception.getMessage(), request.getRequestURI());
        return buildError(
                HttpStatus.CONFLICT,
                messageSource.getMessage("error.already_exists", null, "Already exists", locale),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException exception, HttpServletRequest request, Locale locale) {
        log.warn("Bad state: {} path={}", exception.getMessage(), request.getRequestURI());
        return buildError(
                HttpStatus.BAD_REQUEST,
                messageSource.getMessage("error.bad_request", null, "Bad request", locale),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request, Locale locale) {
        log.warn("Constraint violation path={} count={}", request.getRequestURI(), exception.getConstraintViolations().size());
        List<String> details = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        return buildError(
                HttpStatus.BAD_REQUEST,
                messageSource.getMessage("error.validation", null, "Validation failed", locale),
                messageSource.getMessage("error.validation.details", null, "Invalid request payload", locale),
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception, HttpServletRequest request, Locale locale) {
        log.warn("Access denied path={} message={}", request.getRequestURI(), exception.getMessage());
        return buildError(
                HttpStatus.FORBIDDEN,
                messageSource.getMessage("error.forbidden", null, "Forbidden", locale),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request, Locale locale) {
        log.warn("Validation failed path={} errors={}", request.getRequestURI(), exception.getErrorCount());
        List<String> details = exception.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .toList();
        return buildError(
                HttpStatus.BAD_REQUEST,
                messageSource.getMessage("error.validation", null, "Validation failed", locale),
                messageSource.getMessage("error.validation.details", null, "Invalid request payload", locale),
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception exception, HttpServletRequest request, Locale locale) {
        log.error("Unhandled error path={}", request.getRequestURI(), exception);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                messageSource.getMessage("error.internal", null, "Internal server error", locale),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status, String error, String message, String path, List<String> details) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(body);
    }
}
