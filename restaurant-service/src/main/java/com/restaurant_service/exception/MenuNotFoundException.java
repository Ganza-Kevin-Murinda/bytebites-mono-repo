package com.restaurant_service.exception;

public class MenuNotFoundException extends AppException {
    public MenuNotFoundException(String message) {
        super(message);
    }
}
