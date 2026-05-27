package Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteEndpointManager {

    @DeleteMapping("/api/visa-holders/{id}")
    public ResponseEntity<?> deleteHolder(
            @PathVariable
            Long id){
        //authentication would be here

        try {
            // Need a delete method to pass the id to delete
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
