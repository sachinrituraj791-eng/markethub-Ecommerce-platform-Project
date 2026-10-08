package exception;

/**
 * AuthorizationException
 * Thrown when an authenticated user attempts an operation outside their role permissions
 * (e.g. buyer attempting seller updates, or seller modifying another seller's catalog).
 */
public class AuthorizationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
