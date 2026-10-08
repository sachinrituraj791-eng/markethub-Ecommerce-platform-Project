package exception;

/**
 * ValidationException
 * Thrown when business rules or data constraints are violated.
 * Part of Core Java Exception Handling (10 Marks).
 */
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
