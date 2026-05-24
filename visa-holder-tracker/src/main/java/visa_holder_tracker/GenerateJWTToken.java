package visa_holder_tracker;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class GenerateJWTToken {

    private String secretKey = "";

    public GenerateJWTToken(){
        try {
            // Choosing "HmacSHA256" algorithm to generate keys
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");

            // Generate a random key.
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

    public String generateToken(String username){

        // Initialising and declaring a hashmap
        Map<String, Object> claims = new HashMap<>();

        // return the formed JWT string token
        return Jwts.builder() // Initializing a JWT token build
                .claims() // Initializing claims to add extra data attached to the JWT
                .add(claims) // Add extra data attached to the JWT
                .subject(username) // Set the username as the user identifier
                .issuedAt(new Date(System.currentTimeMillis())) // Set issue date and time as now in milliseconds
                .expiration(new Date(System.currentTimeMillis() + 300000L)) // Set the expiration of the JWT token to 5 minutes
                .and() // Ends the claims builder
                .signWith(getKey()) // Signs the token using a secret private key
                .compact(); // Returns the JWT string token (Generates the token)
    }

    public Key getKey(){
        // Decode the key using base 64 and convert it to the byte datatype
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        // Perform something on the key bytes? And return the key in byte datatype
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
