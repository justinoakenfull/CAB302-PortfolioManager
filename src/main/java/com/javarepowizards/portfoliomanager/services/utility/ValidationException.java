package com.javarepowizards.portfoliomanager.services.utility;

/**
 * Exception thrown when a validation error occurs in input or business logic.
 */
public class ValidationException extends Exception{

    /**
     * Constructs a new ValidationException with the specified detail message.
     *
     * @param message the detail message explaining the validation failure
     */
    public ValidationException(String message) {
        super(message);
    }

}
