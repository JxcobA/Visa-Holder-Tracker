package visa_holder_tracker.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

@RestController
@RequestMapping("/api/visa-holders")
@RequiredArgsConstructor
public class VisaHolderController {

    private final VisaHolderService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<VisaHolder> createVisaHolder(
            @Valid @RequestBody VisaHolderRequest request
            ){
        VisaHolder savedVisaHolder = service.createVisaHolder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedVisaHolder);
    }

    @GetMapping
    public ResponseEntity<Page<VisaHolder>> getAllVisaHolders(
            @RequestParam(defaultValue= "0") int page,
            @RequestParam(defaultValue = "10" )int size
    ){
        return ResponseEntity.ok(service.getAllVisaHolders(page, size));
    }

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

    @GetMapping("/{passportNumber}")
    public ResponseEntity<VisaHolder> getVisaHolderByPassportNumber(
            @PathVariable String passportNumber
    ){
        return ResponseEntity.ok(
                service.getVisaHolderByPassportNumber(passportNumber)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{passportNumber}")
    public ResponseEntity<VisaHolder> updateVisaHolder(
            @PathVariable String passportNumber,
            @Valid @RequestBody VisaHolderRequest request
    ) {
        return ResponseEntity.ok(
                service.updateVisaHolder(passportNumber, request)
        );
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<VisaHolder>> getExpiringSoon(
            // Defaults to 30 if no value provided
            @RequestParam(defaultValue = "30") int days) {

        List<VisaHolder> expiring = service.getExpiringSoon(days);
        return ResponseEntity.ok(expiring);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{passportNumber}")
    public ResponseEntity<?> deleteHolder(
            @PathVariable String passportNumber) {

        service.deleteVisaHolder(passportNumber);

        return ResponseEntity.noContent().build();
    }
}
