package visa_holder_tracker.controller;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReportController {

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/api/reports/summary")
    public ResponseEntity<?> generateReport(){
        // Services:
        //
        // A method to call and get all expired visa holders
        // A method to call and get all overstayed visa holders
        // A method to call and get all active visa holders
        // A method to call and get all expiring soon visa holders
        // A method to generate a report using the above sub-functions
        //
        // A method to call and get an active visa holder
        // A method to call to delete a holder by passing the id. Should affect the database.
        // A method to call to get a list of the holder's movements by passing the holder's id. Should affect the database too
        // A method to call to add movements (Entry / Exit) Should affect the database too
        //
        return ResponseEntity.ok(Map.of());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/api/reports/download/{date}")
    public ResponseEntity<?> downloadReport(
            @PathVariable
            String input
    ){
        return ResponseEntity.ok(Map.of());
    }


}
