package visa_holder_tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import visa_holder_tracker.dto.MovementRequest;
import visa_holder_tracker.dto.MovementResponse;
import visa_holder_tracker.entity.Movement;
import visa_holder_tracker.entity.MovementType;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.exception.ResourceNotFoundException;
import visa_holder_tracker.repository.MovementRepository;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.util.List;


/**
 * Service responsible for managing visa holder
 * movement records.
 *
 * <p>
 * This service:
 * <ul>
 *     <li>Logs entry and exit movement events.</li>
 *     <li>Retrieves movement history for visa holders.</li>
 *     <li>Maps movement entities to response DTOs.</li>
 *     <li>Validates visa holder existence before logging movements.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class MovementService {


    /**
     * Repository used for movement database operations.
     */
    private final MovementRepository movementRepository;

    /**
     * Repository used for visa holder database operations.
     */
    private final VisaHolderRepository visaHolderRepository;


    /**
     * Retrieves all movement records associated
     * with a visa holder passport number.
     *
     * <p>
     * Movement entities are converted into
     * {@link MovementResponse} DTOs before returning.
     * </p>
     *
     * @param passportNumber visa holder passport number
     * @return list of movement response DTOs
     */
    public List<MovementResponse> getMovementsByHolderId(String passportNumber) {
        return movementRepository.findByVisaHolderPassportNumber(passportNumber)
                .stream().map(MovementResponse::from).toList();
    }


    /**
     * Logs a visa holder movement event.
     *
     * <p>
     * Depending on the movement type,
     * the movement is recorded as either:
     * <ul>
     *     <li>An entry event</li>
     *     <li>An exit event</li>
     * </ul>
     * </p>
     *
     * @param request movement request details
     * @return saved movement entity
     * @throws ResourceNotFoundException if the visa holder does not exist
     */
    public Movement logMovement(MovementRequest request) {
        VisaHolder holder = visaHolderRepository.findByPassportNumber(request.getPassportNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Visa Holder Not Found"));

        Movement movement = new Movement();
        movement.setVisaHolder(holder);

        if (request.getType() == MovementType.ENTRY) {
            movement.setEntryDate(request.getDate());
        } else {
            movement.setExitDate(request.getDate());
        }

        return movementRepository.save(movement);
    }
}