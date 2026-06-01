package visa_holder_tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import visa_holder_tracker.dto.MovementRequest;
import visa_holder_tracker.dto.MovementResponse;
import visa_holder_tracker.entity.Movement;
import visa_holder_tracker.entity.MovementType;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.repository.MovementRepository;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Unit tests for {@link MovementService}.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>Entry movement creation.</li>
 *     <li>Exit movement creation.</li>
 *     <li>Visa holder existence validation.</li>
 *     <li>Movement retrieval by passport number.</li>
 *     <li>Correct interaction with repository dependencies.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Mockito is used to isolate service logic
 * without loading the Spring application context.
 * </p>
 */
class MovementServiceTest {

    /**
     * Mocked repository used
     * for movement persistence operations.
     */
    @Mock
    private MovementRepository movementRepository;

    /**
     * Mocked repository used
     * for visa holder lookup operations.
     */
    @Mock
    private VisaHolderRepository visaHolderRepository;

    /**
     * Service under test with
     * mocked dependencies injected automatically.
     */
    @InjectMocks
    private MovementService movementService;

    /**
     * Initializes Mockito mock objects
     * before each test execution.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Verifies that ENTRY movement requests
     * create movement records with entry dates set.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Visa holder lookup.</li>
     *     <li>Correct ENTRY movement mapping.</li>
     *     <li>Movement persistence behavior.</li>
     *     <li>Repository interaction correctness.</li>
     * </ul>
     * </p>
     */
    @Test
    void logMovement_shouldCreateEntryMovement() {
        MovementRequest request = new MovementRequest();
        request.setPassportNumber("A1234567");
        request.setType(MovementType.ENTRY);
        request.setDate(LocalDateTime.of(2026, 5, 29,00,00));

        VisaHolder holder = new VisaHolder();
        holder.setPassportNumber("A1234567");

        Movement savedMovement = new Movement();
        savedMovement.setVisaHolder(holder);
        savedMovement.setEntryDate(request.getDate());

        when(visaHolderRepository.findByPassportNumber("A1234567"))
                .thenReturn(Optional.of(holder));

        when(movementRepository.save(any(Movement.class)))
                .thenReturn(savedMovement);

        Movement result = movementService.logMovement(request);

        assertNotNull(result);
        assertEquals(LocalDateTime.of(2026, 5, 29,00,00), result.getEntryDate());
        assertNull(result.getExitDate());
        assertEquals(holder, result.getVisaHolder());

        verify(visaHolderRepository).findByPassportNumber("A1234567");
        verify(movementRepository).save(any(Movement.class));
    }

    /**
     * Verifies that EXIT movement requests
     * create movement records with exit dates set.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Visa holder lookup.</li>
     *     <li>Correct EXIT movement mapping.</li>
     *     <li>Movement persistence behavior.</li>
     *     <li>Repository interaction correctness.</li>
     * </ul>
     * </p>
     */
    @Test
    void logMovement_shouldCreateExitMovement() {
        MovementRequest request = new MovementRequest();
        request.setPassportNumber("A1234567");
        request.setType(MovementType.EXIT);
        request.setDate(LocalDateTime.of(2026, 5, 30,00,00));

        VisaHolder holder = new VisaHolder();
        holder.setPassportNumber("A1234567");

        Movement savedMovement = new Movement();
        savedMovement.setVisaHolder(holder);
        savedMovement.setExitDate(request.getDate());

        when(visaHolderRepository.findByPassportNumber("A1234567"))
                .thenReturn(Optional.of(holder));

        when(movementRepository.save(any(Movement.class)))
                .thenReturn(savedMovement);

        Movement result = movementService.logMovement(request);

        assertNotNull(result);
        assertEquals(LocalDateTime.of(2026, 5, 30, 0, 0), result.getExitDate());        assertNull(result.getEntryDate());
        assertEquals(holder, result.getVisaHolder());

        verify(visaHolderRepository).findByPassportNumber("A1234567");
        verify(movementRepository).save(any(Movement.class));
    }

    /**
     * Verifies that movement logging fails
     * when the visa holder does not exist.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Exception handling behavior.</li>
     *     <li>Visa holder existence validation.</li>
     *     <li>Prevention of invalid movement persistence.</li>
     * </ul>
     * </p>
     */
    @Test
    void logMovement_shouldThrowExceptionWhenVisaHolderNotFound() {
        MovementRequest request = new MovementRequest();
        request.setPassportNumber("UNKNOWN");
        request.setType(MovementType.ENTRY);
        request.setDate(LocalDateTime.of(2026, 5, 29,00,00));

        when(visaHolderRepository.findByPassportNumber("UNKNOWN"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> movementService.logMovement(request)
        );

        assertEquals("Visa Holder Not Found", exception.getMessage());

        verify(visaHolderRepository).findByPassportNumber("UNKNOWN");
        verify(movementRepository, never()).save(any(Movement.class));
    }

    /**
     * Verifies that movement history
     * can be retrieved using a passport number.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Movement repository query behavior.</li>
     *     <li>Entity-to-response DTO mapping.</li>
     *     <li>Correct movement response generation.</li>
     * </ul>
     * </p>
     */
    @Test
    void getMovementsByHolderId_shouldReturnMovementList() {
        String passportNumber = "A1234567";

        VisaHolder holder = new VisaHolder();
        holder.setPassportNumber(passportNumber);

        Movement movement1 = new Movement();
        movement1.setVisaHolder(holder);                 // needed so from() doesn't NPE
        movement1.setEntryDate(LocalDateTime.of(2026, 5, 29, 0, 0));

        Movement movement2 = new Movement();
        movement2.setVisaHolder(holder);
        movement2.setExitDate(LocalDateTime.of(2026, 5, 30, 0, 0));

        when(movementRepository.findByVisaHolderPassportNumber(passportNumber))
                .thenReturn(List.of(movement1, movement2));

        List<MovementResponse> result = movementService.getMovementsByHolderId(passportNumber);

        assertEquals(2, result.size());
        assertEquals(passportNumber, result.get(0).passportNumber());
        verify(movementRepository).findByVisaHolderPassportNumber(passportNumber);
    }
}