package com.auth_service.exception;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
