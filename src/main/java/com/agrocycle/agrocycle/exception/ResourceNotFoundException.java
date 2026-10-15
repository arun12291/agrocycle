package com.agrocycle.agrocycle.exception;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
