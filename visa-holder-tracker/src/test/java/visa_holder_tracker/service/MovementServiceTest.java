package visa_holder_tracker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import visa_holder_tracker.dto.MovementRequest;
import visa_holder_tracker.entity.Movement;
import visa_holder_tracker.entity.MovementType;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;
import visa_holder_tracker.repository.MovementRepository;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(MovementService.class)
class MovementServiceTest {

    @Autowired
    VisaHolderRepository visaHolderRepository;

    @Autowired
    MovementService movementService;

    @Test
    void logEntry_SavesMovementWithEntryDate() {
        // Arrange: put a holder in the DB
        VisaHolder holder = VisaHolder.builder()
                .passportNumber("A123")
                .fullName("Newlife")
                .nationality("Testland")
                .visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE)
                .build();
        visaHolderRepository.save(holder);

        // build the request that says "log an ENTRY for A123 today"
        MovementRequest request = new MovementRequest();
        request.setPassportNumber("A123");
        request.setType(MovementType.ENTRY);
        request.setDate(LocalDate.now());

        // Act: call the method under test
        Movement result = movementService.logMovement(request);

        // Assert: check it did the right thing
        assertNotNull(result.getId());                 // it was saved (got an id)
        assertEquals(LocalDate.now(), result.getEntryDate());  // entry slot filled
        assertNull(result.getExitDate());              // exit slot left empty
    }

    @Test
    void logEntry_SavesMovementWithExitDate() {
        // Arrange: put a holder in the DB
        VisaHolder holder = VisaHolder.builder()
                .passportNumber("A123")
                .fullName("Newlife")
                .nationality("Testland")
                .visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE)
                .build();
        visaHolderRepository.save(holder);

        // build the request that says "log an ENTRY for A123 today"
        MovementRequest request = new MovementRequest();
        request.setPassportNumber("A123");
        request.setType(MovementType.EXIT);
        request.setDate(LocalDate.now());

        // Act: call the method under test
        Movement result = movementService.logMovement(request);

        // Assert: check it did the right thing
        assertNotNull(result.getId());                 // it was saved (got an id)
        assertEquals(LocalDate.now(), result.getExitDate());  // exit slot filled
        assertNull(result.getEntryDate());            // exit slot left empty
    }
}