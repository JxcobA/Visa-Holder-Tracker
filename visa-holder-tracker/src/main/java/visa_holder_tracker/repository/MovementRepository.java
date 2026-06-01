package visa_holder_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visa_holder_tracker.entity.Movement;

import java.util.List;



/**
 * Repository interface for performing database operations
 * on {@link Movement} entities.
 *
 * <p>
 * Provides CRUD functionality and movement-specific
 * query operations.
 * </p>
 */
public interface MovementRepository extends JpaRepository<Movement, Long> {
    // Uses a Long as Movement uses an auto-incremented numeric ID
    /**
     * Retrieves all movement records associated
     * with a visa holder passport number.
     *
     * @param passportNumber visa holder passport number
     * @return list of movement records
     */
    List<Movement> findByVisaHolderPassportNumber(String passportNumber);
    // Spring navigates the relationship: Movement -> visaHolder -> passportNumber -> SELECT * FROM movement WHERE passport_number =

}