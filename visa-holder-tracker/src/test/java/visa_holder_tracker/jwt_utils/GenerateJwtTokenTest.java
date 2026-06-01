package visa_holder_tracker.jwt_utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.*;


/**
 * Unit tests for {@link GenerateJwtToken}.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>JWT token generation.</li>
 *     <li>JWT structure formatting.</li>
 *     <li>Username extraction from tokens.</li>
 *     <li>Role extraction from tokens.</li>
 *     <li>JWT encode/decode consistency.</li>
 * </ul>
 * </p>
 *
 * <p>
 * These are isolated unit tests and do not require
 * the Spring application context.
 * </p>
 */
class GenerateJwtTokenTest {


    /**
     * JWT service under test.
     */
    private GenerateJwtToken jwtService;

    /**
     * Initializes the JWT service before each test.
     *
     * <p>
     * Uses a Base64-encoded secret key
     * for signing and verifying JWT tokens.
     * </p>
     */
    @BeforeEach
    void setUp() {
        jwtService = new GenerateJwtToken(
                "dGhpcy1pcy1hLXRlc3Qtb25seS1zZWNyZXQta2V5LWZvci1qd3Qtc2lnbmluZy0xMjM0NTY3ODkw");
    }

    /**
     * Helper method for creating mock
     * authenticated users.
     *
     * <p>
     * Builds a Spring Security
     * {@link Authentication} object
     * containing:
     * <ul>
     *     <li>A username</li>
     *     <li>A granted role/authority</li>
     * </ul>
     * </p>
     *
     * @param username authenticated username
     * @param role granted security role
     * @return mock authentication object
     */
    // Helper:
    // - Builds a mock Authentication with a username and role
    private Authentication auth(String username, String role) {
        return new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(role)));
    }

    /**
     * Verifies that generated JWT tokens
     * are successfully created and non-empty.
     *
     * <p>
     * Ensures token generation produces
     * a valid encoded JWT string.
     * </p>
     */
    @Test
    void generateToken_returnsNonNullString() {
        String token = jwtService.generateToken(auth("Admin User", "ROLE_ADMIN"));
        assertThat(token).isNotBlank();
    }

    /**
     * Verifies that generated tokens
     * follow standard JWT structure formatting.
     *
     * <p>
     * JWT tokens use the format:
     * {@code header.payload.signature}
     * which contains exactly two dot separators.
     * </p>
     */
    @Test
    void generateToken_hasTwoDotsIndicatingJwtStructure() {
        // Checks token is from JWT by seeing if it matches correct structure: header.payload.signature
        String token = jwtService.generateToken(auth("Admin User", "ROLE_ADMIN"));
        assertThat(token.chars().filter(c -> c == '.').count()).isEqualTo(2);
    }

    /**
     * Verifies that usernames
     * can be correctly extracted
     * from generated JWT tokens.
     *
     * <p>
     * Ensures the token subject
     * matches the original username.
     * </p>
     */
    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken(auth("Normal User", "ROLE_USER"));
        assertThat(jwtService.extractUsername(token)).isEqualTo("Normal User");
    }

    /**
     * Verifies that administrator roles
     * can be correctly extracted
     * from generated JWT tokens.
     *
     * <p>
     * Ensures role claims are stored
     * and parsed correctly.
     * </p>
     */
    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtService.generateToken(auth("Admin User", "ROLE_ADMIN"));
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }

    /**
     * Verifies that USER roles
     * can be correctly extracted
     * from generated JWT tokens.
     *
     * <p>
     * Ensures role claim handling
     * works consistently for standard users.
     * </p>
     */
    @Test
    void extractRole_userRole_returnsRoleUser() {
        String token = jwtService.generateToken(auth("Normal User", "ROLE_USER"));
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_USER");
    }

    /**
     * Verifies that usernames remain unchanged
     * after JWT generation and extraction.
     *
     * <p>
     * This test validates the full JWT
     * encode/decode round-trip process.
     * </p>
     */
    @Test
    void extractUsername_afterRoundTrip_matchesOriginal() {
        // Ensures encode/decode happens correctly during generation and extraction;
        String original = "Some Visa Officer";
        String token = jwtService.generateToken(auth(original, "ROLE_USER"));
        assertThat(jwtService.extractUsername(token)).isEqualTo(original);
    }


}