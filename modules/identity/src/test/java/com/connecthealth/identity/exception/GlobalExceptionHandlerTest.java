package com.connecthealth.identity.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiException_conflict_returns409() {
        ConflictException ex = new ConflictException("Email already in use");

        ResponseEntity<?> response = handler.handleApiException(ex);

        assertEquals(409, response.getStatusCode().value());
    }

    @Test
    void handleApiException_unauthorized_returns401() {
        UnauthorizedException ex = new UnauthorizedException("Invalid credentials");

        ResponseEntity<?> response = handler.handleApiException(ex);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void handleApiException_notFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

        ResponseEntity<?> response = handler.handleApiException(ex);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void handleApiException_bodyContainsCodeAndMessage() {
        ConflictException ex = new ConflictException("Email already in use");

        ResponseEntity<?> response = handler.handleApiException(ex);

        assertNotNull(response.getBody());
        String body = response.getBody().toString();
        assertTrue(body.contains("CONFLICT"));
        assertTrue(body.contains("Email already in use"));
    }

    @Test
    void handleValidation_returns400WithFieldDetails() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("registerRequest", "email", "must be a well-formed email address");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<?> response = handler.handleValidation(ex);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        String body = response.getBody().toString();
        assertTrue(body.contains("VALIDATION_ERROR"));
        assertTrue(body.contains("email"));
    }

    @Test
    void handleValidation_fieldErrorWithNullMessage_usesInvalidFallback() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "name", null, false, null, null, null);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<?> response = handler.handleValidation(ex);

        assertEquals(400, response.getStatusCode().value());
        String body = response.getBody().toString();
        assertTrue(body.contains("invalid"));
    }
}
