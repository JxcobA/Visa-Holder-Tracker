package visa_holder_tracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Operation(
            summary = "Returns a summary of different visa statuses.",
            description = "Returns a count of each visa status."
    )
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

    @Operation(
            summary = "Generates a report and uploads to AWS Cloud.",
            description =
                    "Generates a summary report count of each visa status and uploads " +
                    "it remotely to AWS S3 bucket and SQS."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/api/reports/download/{date}")
    public ResponseEntity<?> downloadReport(@PathVariable String date) {
        String presignedUrl = reportService.generateAndUpload(date);
        return ResponseEntity.ok(Map.of("url", presignedUrl));
    }


}
