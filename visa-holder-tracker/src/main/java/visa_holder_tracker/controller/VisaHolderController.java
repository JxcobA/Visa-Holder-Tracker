package visa_holder_tracker.controller;


import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import visa_holder_tracker.dto.VisaHolderRequest;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;
import visa_holder_tracker.service.VisaHolderService;

import java.util.List;

@RestController
@RequestMapping("/api/visa-holders")
public class VisaHolderController {

    private final VisaHolderService service;

    public VisaHolderController(VisaHolderService service) {
        this.service = service;
    }

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
}
