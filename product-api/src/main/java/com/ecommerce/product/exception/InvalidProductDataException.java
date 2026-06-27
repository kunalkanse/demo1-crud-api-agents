package com.ecommerce.product.exception;

/**
 * Exception thrown when product data validation fails.
 */
public class InvalidProductDataException extends RuntimeException {
    public InvalidProductDataException(String message) {
        super(message);
    }

    public InvalidProductDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
