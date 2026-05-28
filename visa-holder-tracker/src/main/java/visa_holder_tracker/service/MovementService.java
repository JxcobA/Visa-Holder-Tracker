package visa_holder_tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import visa_holder_tracker.entity.Movement;
import visa_holder_tracker.repository.MovementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovementService {

    private final MovementRepository movementRepository;

    public List<Movement> getMovementsByHolderId(String passportNumber) {
        return movementRepository.findByVisaHolderPassportNumber(passportNumber);
    }
}