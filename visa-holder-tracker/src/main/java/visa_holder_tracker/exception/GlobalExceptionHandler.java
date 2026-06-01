package visa_holder_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;


/**
 * Global exception handler responsible for managing
 * application-wide REST API exceptions.
 *
 * <p>
 * This handler:
 * <ul>
 *     <li>Intercepts exceptions thrown by controllers and services.</li>
 *     <li>Returns consistent HTTP error responses.</li>
 *     <li>Maps exceptions to appropriate HTTP status codes.</li>
 * </ul>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles resource not found exceptions.
     *
     * <p>
     * Returns an HTTP 404 response when
     * a requested resource cannot be found.
     * </p>
     *
     * @param e thrown exception
     * @return error response containing the exception message
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,String>> notFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    /**
     * Handles authentication failures caused
     * by invalid login credentials.
     *
     * <p>
     * Returns an HTTP 401 Unauthorized response.
     * </p>
     *
     * @param e thrown authentication exception
     * @return error response indicating invalid credentials
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String,String>> badCreds(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
    }
}
