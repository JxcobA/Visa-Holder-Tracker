package visa_holder_tracker.jwt_utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GenerateJWTTokenTest {

    private GenerateJWTToken jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new GenerateJWTToken(
                "dGhpcy1pcy1hLXRlc3Qtb25seS1zZWNyZXQta2V5LWZvci1qd3Qtc2lnbmluZy0xMjM0NTY3ODkw");
    }

    // Helper:
    // - Builds a mock Authentication with a username and role
    private Authentication auth(String username, String role) {
        return new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(role)));
    }


    @Test
    void generateToken_returnsNonNullString() {
        String token = jwtService.generateToken(auth("Admin User", "ROLE_ADMIN"));
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_hasTwoDotsIndicatingJwtStructure() {
        // Checks token is from JWT by seeing if it matches correct structure: header.payload.signature
        String token = jwtService.generateToken(auth("Admin User", "ROLE_ADMIN"));
        assertThat(token.chars().filter(c -> c == '.').count()).isEqualTo(2);
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken(auth("Normal User", "ROLE_USER"));
        assertThat(jwtService.extractUsername(token)).isEqualTo("Normal User");
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtService.generateToken(auth("Admin User", "ROLE_ADMIN"));
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void extractRole_userRole_returnsRoleUser() {
        String token = jwtService.generateToken(auth("Normal User", "ROLE_USER"));
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_USER");
    }

    @Test
    void extractUsername_afterRoundTrip_matchesOriginal() {
        // Ensures encode/decode happens correctly during generation and extraction;
        String original = "Some Visa Officer";
        String token = jwtService.generateToken(auth(original, "ROLE_USER"));
        assertThat(jwtService.extractUsername(token)).isEqualTo(original);
    }


}