package com.restaurant_service.exception;

public class RestaurantNotFoundException extends AppException {
    public RestaurantNotFoundException(String message) {
        super(message);
    }
}
