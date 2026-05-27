package visa_holder_tracker.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/visa-holders")
public class VisaHolderController {


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHolder(
            @PathVariable
            Long id){

        try {
            // Need a delete method to pass the id to delete
            return ResponseEntity.noContent().build();
        } catch (Exception e) {// Exception catcher needs to be changed
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/expiring-soon")
    public ResponseEntity<?> visasExpiringSoon(){
        return ResponseEntity.ok(Map.of());
    }
}
