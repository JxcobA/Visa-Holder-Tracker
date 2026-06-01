package visa_holder_tracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import visa_holder_tracker.dto.MovementRequest;
import visa_holder_tracker.dto.MovementResponse;
import visa_holder_tracker.entity.Movement;
import visa_holder_tracker.service.MovementService;

import java.util.List;


/**
 * REST controller responsible for managing visa holder
 * movement records such as entries and exits.
 *
 * <p>
 * This controller:
 * <ul>
 *     <li>Allows administrators to log movement events.</li>
 *     <li>Allows authenticated users and administrators
 *     to retrieve movement history.</li>
 *     <li>Delegates business logic to the {@link MovementService}.</li>
 * </ul>
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movements")
public class MovementController {

    /**
     * Service responsible for movement-related business logic.
     */
    private final MovementService movementService;


    /**
     * Logs a visa holder movement event.
     *
     * <p>
     * This endpoint allows administrators to record
     * entry or exit events for a visa holder.
     * </p>
     *
     * @param request request containing movement details
     * @return HTTP 201 response containing the saved movement record
     */
    @Operation(
            summary = "Logs entry or exit events for a visa holder.",
            description = "Logs entry or exit events for a visa holder."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Movement> logMovement(
            @Valid @RequestBody MovementRequest request) {
        Movement saved = movementService.logMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    /**
     * Retrieves movement history for a visa holder.
     *
     * <p>
     * This endpoint returns all recorded movement events
     * associated with the provided passport number.
     * </p>
     *
     * @param passportNumber visa holder passport number
     * @return list of movement records for the visa holder
     */
    @Operation(
            summary = "Returns a list of movements.",
            description = "Returns a list of movements for the requested visa holder."
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{passportNumber}")
    public ResponseEntity<List<MovementResponse>> getMovements(
            @PathVariable String passportNumber) {
        return ResponseEntity.ok(movementService.getMovementsByHolderId(passportNumber));
    }
}
