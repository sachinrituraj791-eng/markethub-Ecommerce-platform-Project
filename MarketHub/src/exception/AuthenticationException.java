package exception;

/**
 * AuthenticationException
 * Thrown on bad credentials, expired sessions, or unauthenticated access attempts.
 */
public class AuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
