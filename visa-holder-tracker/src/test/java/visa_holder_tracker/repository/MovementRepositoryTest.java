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


/**
 * Repository integration tests for {@link MovementRepository}.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>Movement persistence behavior.</li>
 *     <li>Movement lookup by visa holder passport number.</li>
 *     <li>Entity relationships between {@link Movement}
 *     and {@link VisaHolder}.</li>
 *     <li>Custom repository query correctness.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Uses {@link DataJpaTest} to load
 * an isolated JPA persistence context
 * with an in-memory test database.
 * </p>
 */
@DataJpaTest
public class MovementRepositoryTest {


    /**
     * Repository under test used
     * for movement database operations.
     */
    @Autowired
    MovementRepository movementRepository;

    /**
     * Repository used for creating
     * visa holder test data.
     *
     * <p>
     * Required because {@link Movement}
     * has a foreign key relationship
     * to {@link VisaHolder}.
     * </p>
     */
    @Autowired
    VisaHolderRepository visaHolderRepository;

    /**
     * Verifies that movements
     * can be retrieved using
     * a visa holder passport number.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Foreign key relationship persistence.</li>
     *     <li>Correct repository query filtering.</li>
     *     <li>Retrieval of multiple movement records.</li>
     * </ul>
     * </p>
     */
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

    /**
     * Verifies that querying with
     * a non-existent passport number
     * returns an empty result list.
     *
     * <p>
     * Ensures repository queries
     * correctly return no matches
     * when movement records do not exist.
     * </p>
     */
    @Test
    void findByVisaHolderPassportNumber_wrongPassport_returnsEmpty() {
        List<Movement> results = movementRepository.findByVisaHolderPassportNumber("WRONG");
        assertThat(results).isEmpty();
    }
}
