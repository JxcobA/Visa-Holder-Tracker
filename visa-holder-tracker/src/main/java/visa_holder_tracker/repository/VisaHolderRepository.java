package visa_holder_tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.entity.VisaStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;




/**
 * Repository interface for performing database operations
 * on {@link VisaHolder} entities.
 *
 * <p>
 * Provides:
 * <ul>
 *     <li>CRUD operations</li>
 *     <li>Pagination support</li>
 *     <li>Filtering and search functionality</li>
 *     <li>Custom JPQL queries for expiry and compliance tracking</li>
 * </ul>
 * </p>
 */
public interface VisaHolderRepository
        extends JpaRepository<VisaHolder, String> {


    /**
     * Retrieves visa holders filtered by visa status.
     *
     * @param status visa status filter
     * @param pageable pagination configuration
     * @return paginated visa holder results
     */
    Page<VisaHolder> findByStatus(
            VisaStatus status,
            Pageable pageable
    );
    // Spring will Generate:
    // SELECT * FROM visa_holders WHERE status = ? LIMIT ? OFFSET ?


    /**
     * Retrieves visa holders by visa type.
     *
     * @param visaType visa category filter
     * @return list of matching visa holders
     */
    List<VisaHolder> findByVisaType(String visaType);


    /**
     * Searches visa holders by full name
     * using case-insensitive partial matching.
     *
     * @param fullName visa holder full name
     * @param pageable pagination configuration
     * @return paginated matching visa holders
     */
    Page<VisaHolder> findByFullNameContainingIgnoreCase(
            String fullName,
            Pageable pageable
    );
    // SELECT * FROM visa_holders WHERE LOWER(full_name) LIKE LOWER ('%?%')


    /**
     * Finds a visa holder by passport number.
     *
     * @param passportNumber visa holder passport number
     * @return optional containing the matching visa holder
     */
    Optional<VisaHolder> findByPassportNumber(String passportNumber);
    // SELECT * FROM visa_holders WHERE passport_number = ?

    /**
     * Counts visa holders by visa status.
     *
     * @param status visa status filter
     * @return total number of matching visa holders
     */
    long countByStatus(VisaStatus status);

    /**
     * Retrieves visa holders whose visas
     * are expiring within a date range.
     *
     * @param today current date and time
     * @param cutoff expiry cutoff date and time
     * @return list of visa holders expiring soon
     */
    @Query("SELECT v FROM VisaHolder v WHERE v.expiryDate BETWEEN :today AND :cutoff")
    List<VisaHolder> findExpiringSoon(
            @Param("today") LocalDateTime today,
            @Param("cutoff") LocalDateTime cutoff
    );
    // This JPQL uses the Java class names, VisaHolder and field name expiryDate rather than DB table names

    /**
     * Retrieves visa holders whose visas
     * have already expired.
     *
     * @param today current date and time
     * @return list of expired visa holders
     */
    @Query("SELECT v FROM VisaHolder v WHERE v.expiryDate < :today")
    List<VisaHolder> findExpired(@Param("today") LocalDateTime today);

    /**
     * Retrieves overstayed visa holders.
     *
     * <p>
     * Overstayed visa holders are identified
     * by expired visas and a matching visa status.
     * </p>
     *
     * @param today current date and time
     * @param status visa status filter
     * @return list of overstayed visa holders
     */
    @Query("SELECT v FROM VisaHolder v WHERE v.expiryDate < :today AND v.status = :status")
    List<VisaHolder> findOverstayed(
            @Param("today") LocalDateTime today,
            @Param("status") VisaStatus status
    );

    /**
     * Retrieves visa holders whose entry dates
     * fall within a specified range.
     *
     * @param start range start date
     * @param end range end date
     * @return list of matching visa holders
     */
    @Query("SELECT v FROM VisaHolder v WHERE v.entryDate >= :start AND v.entryDate < :end")
    List<VisaHolder> findByEntryDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}