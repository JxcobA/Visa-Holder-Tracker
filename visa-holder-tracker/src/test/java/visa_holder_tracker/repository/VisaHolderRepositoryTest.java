package visa_holder_tracker.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import visa_holder_tracker.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Repository integration tests for {@link VisaHolderRepository}.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>Custom repository query correctness.</li>
 *     <li>Visa holder persistence behavior.</li>
 *     <li>Pagination and filtering functionality.</li>
 *     <li>Expiry and overstay query logic.</li>
 *     <li>Status-based repository operations.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Uses {@link DataJpaTest} with an isolated
 * in-memory database for repository testing.
 * </p>
 */
@DataJpaTest
public class VisaHolderRepositoryTest {

    /**
     * Repository under test used
     * for visa holder database operations.
     */
    @Autowired
    VisaHolderRepository visaHolderRepository;

    /**
     * Repository used for movement cleanup
     * because movement records reference
     * visa holders through foreign keys.
     */
    @Autowired
    MovementRepository movementRepository;

    /**
     * Clears test database state before each test.
     *
     * <p>
     * Movement records are deleted first
     * due to foreign key constraints.
     * </p>
     */
    @BeforeEach
    void setUp() {
        movementRepository.deleteAll(); // Deletes child rows first due to FK constraint
        visaHolderRepository.deleteAll();
    }


    /**
     * Helper method for creating reusable
     * visa holder test entities.
     *
     * @param passport passport number
     * @param name visa holder full name
     * @param status visa status
     * @param expiry visa expiry date
     * @return configured visa holder entity
     */
    // Helpers
    private VisaHolder buildHolder(String passport, String name, VisaStatus status,
                                   LocalDateTime expiry) {
        return VisaHolder.builder()
                .passportNumber(passport)
                .fullName(name)
                .nationality("British")
                .visaType("Student")
                .status(status)
                .expiryDate(expiry)
                .entryDate(LocalDateTime.now().minusMonths(1))
                .build();
    }


    /**
     * Verifies that existing passport numbers
     * return matching visa holder records.
     *
     * <p>
     * Ensures repository lookup queries
     * correctly retrieve persisted entities.
     * </p>
     */
    // Tests
    @Test
    void findByPassportNumber_exists_returnsHolder() {
        visaHolderRepository.save(buildHolder("PN001", "Jane Doe", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));

        Optional<VisaHolder> result = visaHolderRepository.findByPassportNumber("PN001");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Jane Doe");
    }

    /**
     * Verifies that unknown passport numbers
     * return empty results.
     *
     * <p>
     * Ensures repository queries correctly handle
     * non-existent records.
     * </p>
     */
    @Test
    void findByPassportNumber_missing_returnsEmpty() {
        Optional<VisaHolder> result = visaHolderRepository.findByPassportNumber("GHOST");
        assertThat(result).isEmpty();
    }

    /**
     * Verifies that partial case-insensitive
     * name searches return matching visa holders.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Partial string matching.</li>
     *     <li>Case-insensitive searching.</li>
     *     <li>Pagination support.</li>
     * </ul>
     * </p>
     */
    @Test
    void findByFullNameContainingIgnoreCase_partialMatch_returnsResults() {
        // Test data for a VisaHolder
        visaHolderRepository.save(buildHolder("PN002", "Jacob Adjei", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));
        visaHolderRepository.save(buildHolder("PN003", "Billy Thompson", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));
        // Create paginated list using partial match
        Page<VisaHolder> result = visaHolderRepository.findByFullNameContainingIgnoreCase("jacob", PageRequest.of(0, 10));
        // Check that
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPassportNumber()).isEqualTo("PN002");
    }

    /**
     * Verifies that unmatched search terms
     * return empty paginated results.
     *
     * <p>
     * Ensures repository search queries
     * correctly return no matches when appropriate.
     * </p>
     */
    @Test
    void findByFullNameContainingIgnoreCase_noMatch_returnsEmpty() {
        visaHolderRepository.save(buildHolder("PN002", "Newlife Igbinedion", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));

        Page<VisaHolder> result = visaHolderRepository.findByFullNameContainingIgnoreCase("nobody", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }


    /**
     * Verifies that filtering by ACTIVE status
     * returns only active visa holders.
     *
     * <p>
     * Ensures repository status filtering
     * excludes non-matching records.
     * </p>
     */
    @Test
    void findByStatus_active_returnsOnlyActive() {
        visaHolderRepository.save(buildHolder("PN004", "Active Alice", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));
        visaHolderRepository.save(buildHolder("PN005", "Expired Bob", VisaStatus.EXPIRED, LocalDateTime.now().minusDays(10)));

        Page<VisaHolder> result = visaHolderRepository.findByStatus(VisaStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Active Alice");
    }

    /**
     * Verifies that status counts
     * are calculated correctly.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Status aggregation queries.</li>
     *     <li>Correct ACTIVE counts.</li>
     *     <li>Correct EXPIRED counts.</li>
     * </ul>
     * </p>
     */
    @Test
    void countByStatus_returnsCorrectCount() {
        visaHolderRepository.save(buildHolder("PN006", "Person 6", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));
        visaHolderRepository.save(buildHolder("PN007", "Person 7", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));
        visaHolderRepository.save(buildHolder("PN008", "Person 8", VisaStatus.EXPIRED, LocalDateTime.now().minusDays(1)));

        assertThat(visaHolderRepository.countByStatus(VisaStatus.ACTIVE)).isEqualTo(2);
        assertThat(visaHolderRepository.countByStatus(VisaStatus.EXPIRED)).isEqualTo(1);
    }

    /**
     * Verifies that visa holders expiring
     * within the provided date window
     * are correctly returned.
     *
     * <p>
     * Ensures repository expiry queries
     * correctly filter future expiry dates.
     * </p>
     */
    @Test
    void findExpiringSoon_withinWindow_returnsHolder() {
        // Expiring in 15 days
        visaHolderRepository.save(buildHolder("PN009", "Soon Sam", VisaStatus.ACTIVE, LocalDateTime.now().plusDays(15)));
        // Expiring in 60 days - shouldn't be returned
        visaHolderRepository.save(buildHolder("PN010", "Later Larry", VisaStatus.ACTIVE, LocalDateTime.now().plusDays(60)));

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime cutoff = today.plusDays(30);

        List<VisaHolder> result = visaHolderRepository.findExpiringSoon(today, cutoff);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("Soon Sam");
    }

    /**
     * Verifies that no visa holders
     * are returned when none fall
     * within the expiry window.
     *
     * <p>
     * Ensures expiry filtering
     * excludes out-of-range records.
     * </p>
     */
    @Test
    void findExpiringSoon_noneInWindow_returnsEmpty() {
        visaHolderRepository.save(buildHolder("PN011", "Far Future", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(2)));

        List<VisaHolder> result = visaHolderRepository.findExpiringSoon(LocalDateTime.now(), LocalDateTime.now().plusDays(30));

        assertThat(result).isEmpty();
    }


    /**
     * Verifies that expired visa holders
     * are correctly identified.
     *
     * <p>
     * This test validates repository queries
     * using expiry dates earlier than the current time.
     * </p>
     */
    @Test
    void findExpired_pastExpiry_returnsHolder() {
        visaHolderRepository.save(buildHolder("PN012", "Expired Eric", VisaStatus.EXPIRED, LocalDateTime.now().minusDays(5)));
        visaHolderRepository.save(buildHolder("PN013", "Valid Victor", VisaStatus.ACTIVE, LocalDateTime.now().plusDays(5)));

        List<VisaHolder> result = visaHolderRepository.findExpired(LocalDateTime.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPassportNumber()).isEqualTo("PN012");
    }

    /**
     * Verifies that overstayed visa holders
     * are correctly identified.
     *
     * <p>
     * An overstay is defined as:
     * <ul>
     *     <li>A visa holder with an expired visa date.</li>
     *     <li>A visa holder still marked as ACTIVE.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Ensures repository queries exclude
     * already processed EXPIRED records.
     * </p>
     */
    @Test
    void findOverstayed_expiredButStillActive_returnsHolder() {
        // Expired date but status still ACTIVE: overstay
        visaHolderRepository.save(buildHolder("PN014", "Overstay Oliver", VisaStatus.ACTIVE, LocalDateTime.now().minusDays(10)));
        // Expired and status EXPIRED: Not an overstay (by this query's definition at least)
        visaHolderRepository.save(buildHolder("PN015", "Processed Paula", VisaStatus.EXPIRED, LocalDateTime.now().minusDays(10)));

        List<VisaHolder> result = visaHolderRepository.findOverstayed(LocalDateTime.now(), VisaStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPassportNumber()).isEqualTo("PN014");
    }
}