package Controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/movements")
public class LogMovementEndpointManager {

    @GetMapping("/{holderId}")
    public List<String> movementEvent(
            @PathVariable // Binds url (holderId) to a Java class
            Long holderId){

        return null; // Change this such that it calls a get method. (For me Later)

        // check with billy how who can implement this if him or you:
        // Task 6 - Assigned to billy links with this.
        // I need a get method that takes the holderId as a parameter,
        // retrieves and returns its movement history using the List<String>
        // datatype.



    }

}
