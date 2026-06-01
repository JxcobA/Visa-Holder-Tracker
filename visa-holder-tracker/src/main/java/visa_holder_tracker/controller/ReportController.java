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


/**
 * REST controller responsible for generating visa-related reports.
 *
 * <p>
 * This controller:
 * <ul>
 *     <li>Provides visa status summary reports.</li>
 *     <li>Generates downloadable reports.</li>
 *     <li>Uploads generated reports to AWS cloud services.</li>
 *     <li>Returns pre-signed download URLs for generated reports.</li>
 * </ul>
 * </p>
 */
@RestController
@RequiredArgsConstructor
public class ReportController {


    /**
     * Service responsible for visa holder operations
     * and visa status calculations.
     */
    @Autowired
    private final VisaHolderService service;


    /**
     * Service responsible for generating,
     * uploading, and managing reports.
     */
    @Autowired
    private final ReportService reportService;


    /**
     * Generates a summary report of visa statuses.
     *
     * <p>
     * The summary includes:
     * <ul>
     *     <li>Active visas</li>
     *     <li>Expired visas</li>
     *     <li>Overstayed visas</li>
     *     <li>Visas expiring soon</li>
     * </ul>
     * </p>
     *
     * @return HTTP 200 response containing visa status statistics
     */
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


    /**
     * Generates a downloadable report and uploads it to AWS cloud services.
     *
     * <p>
     * The generated report is:
     * <ul>
     *     <li>Uploaded to an AWS S3 bucket.</li>
     *     <li>Processed through AWS SQS messaging.</li>
     *     <li>Returned as a pre-signed download URL.</li>
     * </ul>
     * </p>
     *
     * @param date report generation date identifier
     * @return HTTP 200 response containing the pre-signed download URL
     */
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
