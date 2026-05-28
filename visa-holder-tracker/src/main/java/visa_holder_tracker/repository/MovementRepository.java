package visa_holder_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visa_holder_tracker.entity.Movement;

import java.util.List;

public interface MovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findByVisaHolderPassportNumber(String passportNumber);
}