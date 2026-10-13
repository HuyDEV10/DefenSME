package com.defensme.auth.api;

import com.defensme.auth.security.ProblemResponseWriter;
import com.defensme.auth.service.InvalidCredentialsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    private final ProblemResponseWriter problemWriter;

    public AuthExceptionHandler(ObjectMapper objectMapper, Clock clock) {
        this.problemWriter = new ProblemResponseWriter(objectMapper, clock);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public void invalidCredentials(HttpServletRequest request, HttpServletResponse response) throws IOException {
        problemWriter.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                "Authentication failed",
                "The supplied credentials are invalid."
        );
    }

    @ExceptionHandler(InvalidAuthenticatedUserException.class)
    public void invalidUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        problemWriter.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED.value(),
                "AUTHENTICATION_REQUIRED",
                "Authentication required",
                "A valid access token is required."
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void validation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        problemWriter.write(
                request,
                response,
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Validation failed",
                "One or more fields are invalid."
        );
    }
}
