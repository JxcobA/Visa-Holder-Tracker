package visa_holder_tracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import visa_holder_tracker.jwt_utils.GenerateJwtToken;

import java.util.Map;



@RestController
@RequestMapping("/api/auth") // Authentication endpoint path in localhost
public class AuthenticationController {

    private final GenerateJwtToken jwtService; // JWT bean service
    private final AuthenticationManager authManager; // Authentication Manager bean service
    public record LoginRequest(String username, String password) {} // Credentials record holder variable

    public AuthenticationController(GenerateJwtToken jwtService, AuthenticationManager authManager) {
        this.jwtService = jwtService; // Declare the JWT service in the class
        this.authManager = authManager; // Declare the JWT service in the class
    }

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