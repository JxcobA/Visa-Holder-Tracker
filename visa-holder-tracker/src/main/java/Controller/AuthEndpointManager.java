package Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import visa_holder_tracker.GenerateJWTToken;

import java.util.Map;


@RestController
@RequestMapping("/api/auth") // Authentication endpoint path in localhost
public class AuthEndpointManager {

    private final GenerateJWTToken jwtService; // JWT bean service
    private final AuthenticationManager authManager; // Authentication Manager bean service
    public record LoginRequest(String username, String password) {} // Credentials record holder variable


    public AuthEndpointManager(GenerateJWTToken jwtService, AuthenticationManager authManager) {
        this.jwtService = jwtService; // Declare the JWT service in the class
        this.authManager = authManager; // Declare the JWT service in the class
    }

    @PostMapping("/test") // For testing authentication endpoint features
    public String testEndpoint(
            @RequestBody // Binds the client request body to a java class
            LoginRequest input){

        // Verify user credentials, how is the JWT involved here?
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(input.username(), input.password()));

        // If auth is true the user has been verified.
        if (auth.isAuthenticated())

            // Response body is success if the user is verified.
            return "Success";

        // Response body is fail if the user is not verified.
        return "Failed";
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginEndpoint(
            @RequestBody // Binds the client request body to a java class
            LoginRequest input){

        // Generate a JWT token for the username
        String token = jwtService.generateToken(input.username());

        // Respond with status 200 and the generated JWT token
        return ResponseEntity.ok(Map.of("token", token));
    }
}