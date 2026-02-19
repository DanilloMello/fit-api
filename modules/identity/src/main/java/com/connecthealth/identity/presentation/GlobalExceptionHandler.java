package com.connecthealth.identity.presentation;

import com.connecthealth.identity.domain.exception.EmailAlreadyExistsException;
import com.connecthealth.identity.domain.exception.InvalidCredentialsException;
import com.connecthealth.identity.domain.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return new ErrorResponse(new ErrorDto("EMAIL_CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidTokenException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(RuntimeException ex) {
        return new ErrorResponse(new ErrorDto("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a
                ));
        return new ErrorResponse(new ErrorDto("VALIDATION_ERROR", fieldErrors.toString()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        return new ErrorResponse(new ErrorDto("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    public record ErrorResponse(ErrorDto error) {}
    public record ErrorDto(String code, String message) {}
}
