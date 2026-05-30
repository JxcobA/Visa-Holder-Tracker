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

@DataJpaTest
public class VisaHolderRepositoryTest {

    @Autowired
    VisaHolderRepository visaHolderRepository;

    @Autowired
    MovementRepository movementRepository;

    @BeforeEach
    void setUp() {
        movementRepository.deleteAll(); // Deletes child rows first due to FK constraint
        visaHolderRepository.deleteAll();
    }

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


    // Tests
    @Test
    void findByPassportNumber_exists_returnsHolder() {
        visaHolderRepository.save(buildHolder("PN001", "Jane Doe", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));

        Optional<VisaHolder> result = visaHolderRepository.findByPassportNumber("PN001");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void findByPassportNumber_missing_returnsEmpty() {
        Optional<VisaHolder> result = visaHolderRepository.findByPassportNumber("GHOST");
        assertThat(result).isEmpty();
    }

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

    @Test
    void findByFullNameContainingIgnoreCase_noMatch_returnsEmpty() {
        visaHolderRepository.save(buildHolder("PN002", "Newlife Igbinedion", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));

        Page<VisaHolder> result = visaHolderRepository.findByFullNameContainingIgnoreCase("nobody", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByStatus_active_returnsOnlyActive() {
        visaHolderRepository.save(buildHolder("PN004", "Active Alice", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));
        visaHolderRepository.save(buildHolder("PN005", "Expired Bob", VisaStatus.EXPIRED, LocalDateTime.now().minusDays(10)));

        Page<VisaHolder> result = visaHolderRepository.findByStatus(VisaStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Active Alice");
    }


    @Test
    void countByStatus_returnsCorrectCount() {
        visaHolderRepository.save(buildHolder("PN006", "Person 6", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));
        visaHolderRepository.save(buildHolder("PN007", "Person 7", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(1)));
        visaHolderRepository.save(buildHolder("PN008", "Person 8", VisaStatus.EXPIRED, LocalDateTime.now().minusDays(1)));

        assertThat(visaHolderRepository.countByStatus(VisaStatus.ACTIVE)).isEqualTo(2);
        assertThat(visaHolderRepository.countByStatus(VisaStatus.EXPIRED)).isEqualTo(1);
    }

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

    @Test
    void findExpiringSoon_noneInWindow_returnsEmpty() {
        visaHolderRepository.save(buildHolder("PN011", "Far Future", VisaStatus.ACTIVE, LocalDateTime.now().plusYears(2)));

        List<VisaHolder> result = visaHolderRepository.findExpiringSoon(LocalDateTime.now(), LocalDateTime.now().plusDays(30));

        assertThat(result).isEmpty();
    }

    @Test
    void findExpired_pastExpiry_returnsHolder() {
        visaHolderRepository.save(buildHolder("PN012", "Expired Eric", VisaStatus.EXPIRED, LocalDateTime.now().minusDays(5)));
        visaHolderRepository.save(buildHolder("PN013", "Valid Victor", VisaStatus.ACTIVE, LocalDateTime.now().plusDays(5)));

        List<VisaHolder> result = visaHolderRepository.findExpired(LocalDateTime.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPassportNumber()).isEqualTo("PN012");
    }

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