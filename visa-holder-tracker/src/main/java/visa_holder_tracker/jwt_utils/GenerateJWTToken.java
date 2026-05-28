package visa_holder_tracker.jwt_utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

// A service bean class
@Service
public class GenerateJWTToken {

    // Base64 encoded secret key for signing the token
    private String secretKey;

    public GenerateJWTToken(){
        try {
            // Choosing "HmacSHA256" algorithm to generate keys
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");

            // Generate a random secret key.
            SecretKey sk = keyGen.generateKey();


            // If "HmacSHA256" is the algorithm to create keys.
            // Then what algorithm is sk.getEncoded() using to encode itself?

            // Encode the key and encode and convert the encoded key to string using base 64.
            secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());

        } catch (NoSuchAlgorithmException e){ // Catch unrecognized algorithms

            // Throw a runtime exception error
            throw new RuntimeException(e);
        }
    }

    public String generateToken(Authentication auth){
        String role = auth.getAuthorities().iterator().next().getAuthority();

        // return the formed JWT string token
        return Jwts.builder() // Initializing a JWT token build
                .subject(auth.getName()) // Set the username as the identifier
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis())) // Set issue date and time as now in milliseconds
                .expiration(new Date(System.currentTimeMillis() + 300000L)) // Set the expiration of the JWT token to 5 minutes
                .signWith(getKey()) // Signs the token using a secret private key
                .compact(); // Returns the JWT string token (Generates the token)
    }

    public String extractUsername(String token) {
        // Reads the token to return the username.
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractRole(String token) {
        // Reads the token to return the role.
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public Key getKey(){
        // Decode the key using base 64 and convert it to the byte datatype
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        // Perform something on the key bytes? And return the key in byte datatype
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
