package org.example;

/**
 * Exception thrown when electricity price data cannot be found or retrieved.
 * This typically occurs when data is unavailable for the requested date and region.
 */
public class ElprisNotFoundException extends RuntimeException {
    /**
     * Constructs a new ElprisNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining why the exception was thrown
     */
    public ElprisNotFoundException(String message) {
        super(message);
    }
}
