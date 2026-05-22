package Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import visa_holder_tracker.GenerateJWTToken;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class EndpointManager {

    private final GenerateJWTToken jwtService;
    private final AuthenticationManager authManager;
    public record LoginRequest(String username, String password) {}


    public EndpointManager(GenerateJWTToken jwtService, AuthenticationManager authManager) {
        this.jwtService = jwtService;
        this.authManager = authManager;
    }

    @PostMapping("/test")
    public String testEndpoint(
            @RequestBody
            LoginRequest input){

        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(input.username(), input.password()));
        if (auth.isAuthenticated())
            return "Success";

        return "Failed";
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginEndpoint(
            @RequestBody
            LoginRequest input){
        String token = jwtService.generateToken(input.username());
        return ResponseEntity.ok(Map.of("token", token));
    }
}