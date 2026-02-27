package com.connecthealth.identity.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
