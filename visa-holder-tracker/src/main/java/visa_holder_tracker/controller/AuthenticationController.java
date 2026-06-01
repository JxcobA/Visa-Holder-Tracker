package visa_holder_tracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import visa_holder_tracker.jwt_utils.GenerateJwtToken;

import java.util.Map;



/**
 * REST controller responsible for handling authentication requests.
 *
 * <p>
 * This controller:
 * <ul>
 *     <li>Authenticates users and administrators.</li>
 *     <li>Validates login credentials using Spring Security.</li>
 *     <li>Generates JWT tokens for authenticated users.</li>
 *     <li>Returns authentication tokens to the client.</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/auth") // Authentication endpoint path in localhost
public class AuthenticationController {


    /**
     * Service responsible for generating JWT tokens.
     */
    private final GenerateJwtToken jwtService; // JWT bean service

    /**
     * Spring Security authentication manager used
     * to validate user credentials.
     */
    private final AuthenticationManager authManager; // Authentication Manager bean service

    /**
     * Record used to hold login request credentials.
     *
     * @param username account username
     * @param password account password
     */

    public record LoginRequest(String username, String password) {} // Credentials record holder variable


    /**
     * Constructor used for dependency injection
     * of authentication-related services.
     *
     * @param jwtService JWT generation service
     * @param authManager Spring Security authentication manager
     */
    public AuthenticationController(GenerateJwtToken jwtService, AuthenticationManager authManager) {
        this.jwtService = jwtService; // Declare the JWT service in the class
        this.authManager = authManager; // Declare the JWT service in the class
    }


    /**
     * Authenticates a user and generates a JWT token.
     *
     * <p>
     * The endpoint:
     * <ul>
     *     <li>Accepts username and password credentials.</li>
     *     <li>Authenticates the user using Spring Security.</li>
     *     <li>Generates a JWT token upon successful authentication.</li>
     *     <li>Returns the generated token to the client.</li>
     * </ul>
     * </p>
     *
     * @param input login request containing username and password
     * @return HTTP 200 response containing the generated JWT token
     */
    @Operation(
            summary = "Login to a user or admin account.",
            description = "A JWT token is generated for the client."
    )
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginEndpoint(
            @RequestBody // Binds the client request body to a java class
            LoginRequest input){

        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(input.username(), input.password()));

        // Generate a JWT token for the username
        String token = jwtService.generateToken(auth);

        // Respond with status 200 and the generated JWT token
        return ResponseEntity.ok(Map.of("token", token));
    }
}