package com.order_service.exception;

public class PriceValidationException extends RuntimeException {
    public PriceValidationException(String message) {
        super(message);
    }
}
