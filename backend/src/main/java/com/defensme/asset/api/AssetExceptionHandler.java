package com.defensme.asset.api;

import com.defensme.asset.service.AssetAlreadyExistsException;
import com.defensme.asset.service.AssetNotFoundException;
import com.defensme.asset.service.AssetStateException;
import com.defensme.auth.security.ProblemResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = AssetController.class)
public class AssetExceptionHandler {

    private final ProblemResponseWriter problemWriter;

    public AssetExceptionHandler(ObjectMapper objectMapper, Clock clock) {
        this.problemWriter = new ProblemResponseWriter(objectMapper, clock);
    }

    @ExceptionHandler(AssetNotFoundException.class)
    public void notFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(request, response, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found",
                "The requested asset was not found.");
    }

    @ExceptionHandler(AssetAlreadyExistsException.class)
    public void conflict(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(request, response, HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS", "Resource already exists",
                "An active asset with the same type and identifier already exists.");
    }

    @ExceptionHandler(AssetStateException.class)
    public void invalidState(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(request, response, HttpStatus.CONFLICT, "STATE_TRANSITION_NOT_ALLOWED", "Invalid asset state",
                "The requested asset state transition is not allowed.");
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public void validation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(request, response, HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
                "One or more fields are invalid.");
    }

    private void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String title,
            String detail
    ) throws IOException {
        problemWriter.write(request, response, status.value(), code, title, detail);
    }
}
