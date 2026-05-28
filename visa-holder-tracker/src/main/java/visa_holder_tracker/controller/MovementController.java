package visa_holder_tracker.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequestMapping("api/movements")
public class MovementController {

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{holderId}")
    public List<String> movementEvent(
            @PathVariable // Binds url (holderId) to a Java class
            Long holderId){

        return null; // Change this such that it calls a get method.

    }

}
