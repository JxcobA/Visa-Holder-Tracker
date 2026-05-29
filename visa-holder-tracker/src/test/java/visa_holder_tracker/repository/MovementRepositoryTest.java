package visa_holder_tracker.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import visa_holder_tracker.entity.Movement;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MovementRepositoryTest {

    @Autowired
    MovementRepository movementRepository;

    @Autowired
    VisaHolderRepository visaHolderRepository;

    // Tests
    @Test
    void findByVisaHolderPassportNumber_returnsCorrectMovements() {
        // Save the VisaHolder first (This is a FK constraint)
        VisaHolder holder = VisaHolder.builder()
                .passportNumber("PN099")
                .fullName("Travel Tom")
                .nationality("Irish")
                .visaType("Work")
                .status(VisaStatus.ACTIVE)
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now().minusMonths(3))
                .build();
        visaHolderRepository.save(holder);

        Movement entry = new Movement();
        entry.setVisaHolder(holder);
        entry.setEntryDate(LocalDateTime.now().minusMonths(3));
        movementRepository.save(entry);

        Movement exit = new Movement();
        exit.setVisaHolder(holder);
        exit.setExitDate(LocalDateTime.now().minusMonths(1));
        movementRepository.save(exit);

        List<Movement> results = movementRepository.findByVisaHolderPassportNumber("PN099");

        assertThat(results).hasSize(2);
    }

    @Test
    void findByVisaHolderPassportNumber_wrongPassport_returnsEmpty() {
        List<Movement> results = movementRepository.findByVisaHolderPassportNumber("WRONG");
        assertThat(results).isEmpty();
    }
}
