package com.restaurant_service.exception;

public abstract class AppException extends RuntimeException {
    public AppException(String message){
        super(message);
    }
}
