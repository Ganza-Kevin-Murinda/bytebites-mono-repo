package com.auth_service.exception;

/**
 * Base exception class for all custom application exceptions
 */
public abstract class AuthException extends RuntimeException {

  protected AuthException(String message) {
    super(message);
  }

  protected AuthException(String message, Throwable cause) {
    super(message, cause);
  }
}
