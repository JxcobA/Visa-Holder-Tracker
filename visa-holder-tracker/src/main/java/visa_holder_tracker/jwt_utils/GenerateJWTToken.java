package visa_holder_tracker.jwt_utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

// A service bean class
@Service
public class GenerateJWTToken {

    // Base64 encoded secret key for signing the token
    private final SecretKey secretKey;

    public GenerateJWTToken(@Value("${jwt.secretkey}") String secretKey){
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

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

    public String extractUsername(String token) {
        // Reads the token to return the username.
        return Jwts.parser()
                .verifyWith((SecretKey) this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

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
