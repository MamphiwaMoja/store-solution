package com.example.store.web.rest.errors;

import com.example.store.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionTranslator {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.VALIDATION_FAILED, request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.VALIDATION_FAILED, request, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.MALFORMED_JSON_REQUEST,
                request,
                resolveNotReadableDetails(exception));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        Map<String, String> details = Map.of(
                "parameter", exception.getName(),
                "rejectedValue", String.valueOf(exception.getValue()));
        return build(
                HttpStatus.BAD_REQUEST,
                "Invalid value '%s' for parameter '%s'".formatted(exception.getValue(), exception.getName()),
                request,
                details);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Data integrity violation for path={}", request.getRequestURI(), exception);
        return build(HttpStatus.CONFLICT, ErrorMessages.DATABASE_CONSTRAINT_VIOLATION, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception for path={}", request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.UNEXPECTED_SERVER_ERROR, request, null);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request, Object details) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI(), details);
        return ResponseEntity.status(status).body(response);
    }

    private Object resolveNotReadableDetails(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getMostSpecificCause();
        if (!(cause instanceof InvalidFormatException invalidFormat)) {
            return null;
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("field", resolveFieldName(invalidFormat));
        details.put("rejectedValue", invalidFormat.getValue());
        return details;
    }

    private String resolveFieldName(JsonMappingException exception) {
        return exception.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(fieldName -> fieldName != null && !fieldName.isBlank())
                .findFirst()
                .orElse("request");
    }
}
