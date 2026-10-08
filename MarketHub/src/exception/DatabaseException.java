package exception;

/**
 * DatabaseException
 * Wraps low-level JDBC SQLExceptions to avoid exposing internal SQL schemas or stack traces.
 */
public class DatabaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
