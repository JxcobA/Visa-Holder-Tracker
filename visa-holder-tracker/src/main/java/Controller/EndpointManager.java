package Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class EndpointManager {

    @PostMapping("/api/auth/login")
    public ResponseEntity<String> loginEndpoint(
            @RequestBody
            String input){

        System.out.println(input);

        return ResponseEntity.ok().body("Cool");
    }
}