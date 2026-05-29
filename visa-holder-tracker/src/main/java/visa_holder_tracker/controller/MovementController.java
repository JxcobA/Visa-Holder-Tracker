package visa_holder_tracker.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import visa_holder_tracker.dto.MovementRequest;
import visa_holder_tracker.entity.Movement;
import visa_holder_tracker.service.MovementService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movements")
public class MovementController {

    private final MovementService movementService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Movement> logMovement(
            @Valid @RequestBody MovementRequest request) {
        Movement saved = movementService.logMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{passportNumber}")
    public ResponseEntity<List<Movement>> getMovements(
            @PathVariable String passportNumber) {
        return ResponseEntity.ok(movementService.getMovementsByHolderId(passportNumber));
    }
}
