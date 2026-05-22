package visa_holder_tracker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.service.VisaHolderService;

import java.util.List;

@RestController
@RequestMapping("/api/visa-holders")
@RequiredArgsConstructor
public class VisaHolderController {

    private final VisaHolderService service;

    public VisaHolderController(VisaHolderService service) {
        this.service = service;
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<VisaHolder>> getExpiringSoon(
            // Defaults to 30 if no value provided
            @RequestParam(defaultValue = "30") int days) {

        List<VisaHolder> expiring = service.getExpiringSoon(days);
        return ResponseEntity.ok(expiring);
    }
}
