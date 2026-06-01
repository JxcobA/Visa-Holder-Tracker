package visa_holder_tracker.exception;


/**
 * Custom exception thrown when a requested
 * resource cannot be found in the system.
 *
 * <p>
 * Commonly used when:
 * <ul>
 *     <li>A visa holder does not exist</li>
 *     <li>A movement record is missing</li>
 *     <li>A requested entity cannot be located</li>
 * </ul>
 * </p>
 *
 * <p>
 * This exception is handled globally by
 * the {@code GlobalExceptionHandler}.
 * </p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new resource not found exception
     * with a custom error message.
     *
     * @param msg exception error message
     */
    public ResourceNotFoundException(String msg) { super(msg); }
}

