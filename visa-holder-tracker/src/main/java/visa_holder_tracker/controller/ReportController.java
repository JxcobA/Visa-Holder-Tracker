package visa_holder_tracker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import visa_holder_tracker.service.ReportService;
import visa_holder_tracker.service.VisaHolderService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReportController {

    @Autowired
    private final VisaHolderService service;

    @Autowired
    private final ReportService reportService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/api/reports/summary")
    public ResponseEntity<?> generateReport(){

        return ResponseEntity.ok(Map.of(
                "active", service.countActive().intValue(),
                "expired", service.getExpired().size(),
                "overstay", service.getOverstayed().size(),
                "expiringSoon", service.getExpiringSoon(30).size()
        ));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/api/reports/download/{date}")
    public ResponseEntity<?> downloadReport(@PathVariable String date) {
        String presignedUrl = reportService.generateAndUpload(date);
        return ResponseEntity.ok(Map.of("url", presignedUrl));
    }


}
