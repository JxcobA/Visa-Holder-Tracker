package visa_holder_tracker.service;


import org.springframework.stereotype.Service;
import visa_holder_tracker.dto.ExpiryAlertResponse;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.exception.ResourceNotFoundException;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import visa_holder_tracker.dto.VisaHolderRequest;

import visa_holder_tracker.entity.VisaStatus;



/**
 * Service responsible for managing visa holder
 * business operations and database interactions.
 *
 * <p>
 * This service provides functionality for:
 * <ul>
 *     <li>Creating visa holder records</li>
 *     <li>Updating visa holder records</li>
 *     <li>Deleting visa holder records</li>
 *     <li>Searching and filtering visa holders</li>
 *     <li>Tracking visa expiry and overstay status</li>
 *     <li>Generating expiry alert responses</li>
 * </ul>
 * </p>
 */
@Service
public class VisaHolderService {

    /**
     * Repository used for visa holder
     * database operations.
     */
    private final VisaHolderRepository repository;

    /**
     * Constructor used for dependency injection
     * of the visa holder repository.
     *
     * @param repository visa holder repository
     */
    public VisaHolderService(VisaHolderRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and stores a new visa holder record.
     *
     * @param request visa holder creation request
     * @return saved visa holder entity
     */
    public VisaHolder createVisaHolder(VisaHolderRequest request){
        VisaHolder visaHolder = new VisaHolder();

        visaHolder.setPassportNumber(request.getPassportNumber());
        visaHolder.setFullName(request.getFullName());
        visaHolder.setNationality(request.getNationality());
        visaHolder.setVisaType(request.getVisaType());
        visaHolder.setExpiryDate(request.getExpiryDate());
        visaHolder.setEntryDate(request.getEntryDate());
        visaHolder.setStatus(request.getStatus());

        return repository.save(visaHolder);

    }

    /**
     * Retrieves all visa holders using pagination.
     *
     * @param page page number
     * @param size page size
     * @return paginated visa holder results
     */
    public Page<VisaHolder> getAllVisaHolders(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    /**
     * Searches visa holders by full name
     * using case-insensitive matching.
     *
     * @param fullName visa holder full name
     * @param page page number
     * @param size page size
     * @return paginated matching visa holders
     */
    public Page<VisaHolder> searchVisaHoldersByName(
            String fullName,
            int page,
            int size
    ){
        Pageable pageable = PageRequest.of(page, size);

        return repository.findByFullNameContainingIgnoreCase(
                fullName,
                pageable
        );
    }

    /**
     * Filters visa holders by visa status.
     *
     * @param status visa status filter
     * @param page page number
     * @param size page size
     * @return paginated filtered visa holders
     */
    public Page<VisaHolder> filterVisaHolderByStatus(
            VisaStatus status,
            int page,
            int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByStatus(status, pageable);
    }

    /**
     * Retrieves a visa holder by passport number.
     *
     * @param passportNumber visa holder passport number
     * @return matching visa holder entity
     * @throws ResourceNotFoundException if the visa holder does not exist
     */
    public VisaHolder getVisaHolderByPassportNumber(String passportNumber) {
        return repository.findByPassportNumber(passportNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Visa Holder Not Found"));
    }

    /**
     * Updates an existing visa holder record.
     *
     * @param passportNumber visa holder passport number
     * @param request updated visa holder request
     * @return updated visa holder entity
     * @throws ResourceNotFoundException if the visa holder does not exist
     */
    public VisaHolder updateVisaHolder(String passportNumber, VisaHolderRequest request){
        VisaHolder visaHolder = repository.findByPassportNumber(passportNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Visa Holder Not Found"));

        visaHolder.setFullName(request.getFullName());
        visaHolder.setNationality(request.getNationality());
        visaHolder.setVisaType(request.getVisaType());
        visaHolder.setExpiryDate(request.getExpiryDate());
        visaHolder.setEntryDate(request.getEntryDate());
        visaHolder.setStatus(request.getStatus());

        return repository.save(visaHolder);
    }

    /**
     * Retrieves visa holders whose visas
     * are expiring within the specified number of days.
     *
     * @param days number of days before expiry
     * @return list of expiring visa holders
     */
    // (After merging and testing) AWS notification logic can be written and triggered from here
    public List<VisaHolder> getExpiringSoon(int days) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime cutoff = today.plusDays(days);
        return repository.findExpiringSoon(today, cutoff);
        // Changed to just fetch data
    }

    /**
     * Retrieves all expired visa holders.
     *
     * @return list of expired visa holders
     */
    public List<VisaHolder> getExpired() {
        LocalDateTime today = LocalDateTime.now();
        return repository.findExpired(today);
    }

    /**
     * Deletes a visa holder record.
     *
     * @param passportNumber visa holder passport number
     * @throws ResourceNotFoundException if the visa holder does not exist
     */
    public void deleteVisaHolder(String passportNumber){
        VisaHolder visaHolder = repository.findByPassportNumber(passportNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Visa Holder Not Found"));

        repository.delete(visaHolder);
    }

    /**
     * Counts all visa holders
     * with ACTIVE visa status.
     *
     * @return total active visa count
     */
    public Long countActive() {
        return repository.countByStatus(VisaStatus.ACTIVE);
    }

    /**
     * Retrieves visa holders who have overstayed.
     *
     * <p>
     * Overstayed visa holders are identified
     * as holders whose visas have expired
     * while remaining in ACTIVE status.
     * </p>
     *
     * @return list of overstayed visa holders
     */
    public List<VisaHolder> getOverstayed() {
        LocalDateTime today = LocalDateTime.now();
        return repository.findOverstayed(today, VisaStatus.ACTIVE);
        // Changed to just fetch data
    }

    /**
     * Generates expiry alert information
     * for expiring and expired visa holders.
     *
     * @param days number of days before expiry cutoff
     * @return expiry alert response DTO
     */
    public ExpiryAlertResponse getExpiryAlerts(int days) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime cutoff = today.plusDays(days);
        List<VisaHolder> soon = repository.findExpiringSoon(today, cutoff);
        List<VisaHolder> expired = repository.findExpired(today);
        return new ExpiryAlertResponse(soon, expired);
    }



}


