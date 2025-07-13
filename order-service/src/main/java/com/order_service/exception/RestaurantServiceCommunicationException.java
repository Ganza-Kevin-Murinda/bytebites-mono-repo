package com.order_service.exception;

public class RestaurantServiceCommunicationException extends RuntimeException {
    public RestaurantServiceCommunicationException(String message) {
        super(message);
    }

  public RestaurantServiceCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
