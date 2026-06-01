package visa_holder_tracker.jwt_utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;


/**
 * Service responsible for generating, signing,
 * and parsing JWT authentication tokens.
 *
 * <p>
 * This service:
 * <ul>
 *     <li>Generates JWT tokens for authenticated users.</li>
 *     <li>Signs tokens using a secret cryptographic key.</li>
 *     <li>Extracts usernames from JWT tokens.</li>
 *     <li>Extracts user roles from JWT tokens.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Tokens are signed using an HMAC SHA-based secret key.
 * </p>
 */
@Service
public class GenerateJwtToken {


    /**
     * Secret cryptographic key used
     * to sign and verify JWT tokens.
     */
    private final SecretKey secretKey;


    /**
     * Creates the JWT service and initializes
     * the signing secret key.
     *
     * @param secretKey Base64 encoded JWT secret key
     */
    public GenerateJwtToken(@Value("${jwt.secretkey}") String secretKey){
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }


    /**
     * Generates a signed JWT token for an authenticated user.
     *
     * <p>
     * The generated token contains:
     * <ul>
     *     <li>Username as the subject</li>
     *     <li>User role as a custom claim</li>
     *     <li>Issue timestamp</li>
     *     <li>Expiration timestamp</li>
     * </ul>
     * </p>
     *
     * @param auth authenticated user information
     * @return generated JWT token string
     */
    public String generateToken(Authentication auth){
        String role = auth.getAuthorities().iterator().next().getAuthority();

        // return the formed JWT string token
        return Jwts.builder() // Initializing a JWT token build
                .subject(auth.getName()) // Set the username as the identifier
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis())) // Set issue date and time as now in milliseconds
                .expiration(new Date(System.currentTimeMillis() + 300000L*67)) // Set the expiration of the JWT token to 5 minutes
                .signWith(this.secretKey) // Signs the token using a secret private key
                .compact(); // Returns the JWT string token (Generates the token)
    }


    /**
     * Extracts the username from a JWT token.
     *
     * @param token JWT token string
     * @return username stored in the token subject
     */
    public String extractUsername(String token) {
        // Reads the token to return the username.
        return Jwts.parser()
                .verifyWith((SecretKey) this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }


    /**
     * Extracts the user role from a JWT token.
     *
     * @param token JWT token string
     * @return role stored in the token claims
     */
    public String extractRole(String token) {
        // Reads the token to return the role.
        return Jwts.parser()
                .verifyWith((SecretKey) this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
