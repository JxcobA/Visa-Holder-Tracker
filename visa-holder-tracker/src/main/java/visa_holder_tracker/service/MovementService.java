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

@Service
@RequiredArgsConstructor
public class MovementService {

    private final MovementRepository movementRepository;
    private final VisaHolderRepository visaHolderRepository;

    // This method was made by Jacob, to fix an issue I changed it - Newlife
    public List<MovementResponse> getMovementsByHolderId(String passportNumber) {
        return movementRepository.findByVisaHolderPassportNumber(passportNumber)
                .stream().map(MovementResponse::from).toList();
    }

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