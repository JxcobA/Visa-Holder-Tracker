package visa_holder_tracker.controller;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import visa_holder_tracker.dto.VisaHolderRequest;
import visa_holder_tracker.entity.VisaHolder;
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
    public ResponseEntity<List<VisaHolder>> getAllVisaHolders(){
        return ResponseEntity.ok(service.getAllVisaHolders());
    }
}
