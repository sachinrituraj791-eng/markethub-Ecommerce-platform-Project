package exception;

/**
 * ResourceNotFoundException
 * Thrown when requested entities (product, order, user, category) do not exist in the database.
 */
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
