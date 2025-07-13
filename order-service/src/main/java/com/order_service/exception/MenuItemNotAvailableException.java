package com.order_service.exception;

public class MenuItemNotAvailableException extends RuntimeException {
    public MenuItemNotAvailableException(String message) {
        super(message);
    }
}
