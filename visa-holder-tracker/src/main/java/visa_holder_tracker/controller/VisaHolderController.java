package visa_holder_tracker.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import visa_holder_tracker.dto.ExpiryAlertResponse;
import visa_holder_tracker.dto.VisaHolderRequest;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import visa_holder_tracker.entity.*;
import visa_holder_tracker.service.VisaHolderService;

import java.util.List;


/**
 * REST controller responsible for managing visa holder records.
 *
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *     <li>Creating visa holders</li>
 *     <li>Retrieving visa holder records</li>
 *     <li>Searching and filtering visa holders</li>
 *     <li>Updating visa holder information</li>
 *     <li>Deleting visa holder records</li>
 *     <li>Generating expiry alerts</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/visa-holders")
@RequiredArgsConstructor
public class VisaHolderController {


    /**
     * Service responsible for visa holder business logic
     * and database operations.
     */
    private final VisaHolderService service;


    /**
     * Creates a new visa holder record.
     *
     * @param request visa holder creation request
     * @return HTTP 201 response containing the created visa holder
     */
    @Operation(
            summary = "Create a visa holder",
            description = "Creates a visa holder based on the user request."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<VisaHolder> createVisaHolder(
            @Valid @RequestBody VisaHolderRequest request
            ){
        VisaHolder savedVisaHolder = service.createVisaHolder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedVisaHolder);
    }


    /**
     * Retrieves all visa holders with pagination support.
     *
     * @param page page number
     * @param size number of records per page
     * @return paginated list of visa holders
     */
    @Operation(
            summary = "Get all the visa holders",
            description = "Get all the exiting visa holders in the database."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<VisaHolder>> getAllVisaHolders(
            @RequestParam(defaultValue= "0") int page,
            @RequestParam(defaultValue = "10" )int size
    ){
        return ResponseEntity.ok(service.getAllVisaHolders(page, size));
    }


    /**
     * Searches visa holders by name.
     *
     * @param name visa holder name
     * @param page page number
     * @param size number of records per page
     * @return paginated search results
     */
    @Operation(
            summary = "Search for a visa holder by name.",
            description = "Search for a visa holder in the DB by name."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<Page<VisaHolder>> searchVisaHoldersByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ){
        return ResponseEntity.ok(
                service.searchVisaHoldersByName(name, page, size)
        );
    }


    /**
     * Filters visa holders by visa status.
     *
     * @param status visa status filter
     * @param page page number
     * @param size number of records per page
     * @return paginated filtered visa holders
     */
    @Operation(
            summary = "Get visa holder by filtering visa status.",
            description = "Get visa holders by filtering visa status as requested by the user."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/filter")
    public ResponseEntity<Page<VisaHolder>> filterByStatus(
            @RequestParam VisaStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(
                service.filterVisaHolderByStatus(status, page, size)
        );
    }


    /**
     * Retrieves a visa holder by passport number.
     *
     * @param passportNumber visa holder passport number
     * @return matching visa holder record
     */
    @Operation(
            summary = "Get visa holder by passport number.",
            description = "The client gets a visa holder by passing the passport number."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{passportNumber}")
    public ResponseEntity<VisaHolder> getVisaHolderByPassportNumber(
            @PathVariable String passportNumber
    ){
        return ResponseEntity.ok(
                service.getVisaHolderByPassportNumber(passportNumber)
        );
    }


    /**
     * Updates an existing visa holder record.
     *
     * @param passportNumber visa holder passport number
     * @param request updated visa holder information
     * @return updated visa holder record
     */
    @Operation(
            summary = "Update visa holder.",
            description = "Update a visa holder's record by passing their passport number and record changes."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{passportNumber}")
    public ResponseEntity<VisaHolder> updateVisaHolder(
            @PathVariable String passportNumber,
            @Valid @RequestBody VisaHolderRequest request
    ) {
        return ResponseEntity.ok(
                service.updateVisaHolder(passportNumber, request)
        );
    }


    /**
     * Retrieves visa holders whose visas are expiring soon.
     *
     * @param days number of days before expiry
     * @return list of expiring visa holders
     */
    @Operation(
            summary = "Get expiring soon visa holders.",
            description = "Returns all expiring soon visa holders within 30 days or the user can pass the days."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<VisaHolder>> getExpiringSoon(
            // Defaults to 30 if no value provided
            @RequestParam(defaultValue = "30") int days) {

        List<VisaHolder> expiring = service.getExpiringSoon(days);
        return ResponseEntity.ok(expiring);
    }


    /**
     * Deletes a visa holder record from the database.
     *
     * @param passportNumber visa holder passport number
     * @return HTTP 204 no content response
     */
    @Operation(
            summary = "Deletes a visa holder record from database.",
            description = "The client sends the passport number deletes a visa holder in database."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{passportNumber}")
    public ResponseEntity<?> deleteHolder(
            @PathVariable String passportNumber) {

        service.deleteVisaHolder(passportNumber);

        return ResponseEntity.noContent().build();
    }


    /**
     * Retrieves expiry alerts for visa holders
     * whose visas are expiring soon.
     *
     * @param days number of days before expiry
     * @return expiry alert response containing visa alerts
     */
    @Operation(
            summary = "Get an expiring soon alert for expiring soon visa holders.",
            description = "Returns all alerts for each expiring soon visa holder " +
                    "within 30 days or as set by the client."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/expiry-alerts")
    public ResponseEntity<ExpiryAlertResponse> getExpiryAlerts(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(service.getExpiryAlerts(days));
    }

}
