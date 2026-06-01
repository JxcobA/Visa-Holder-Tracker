package visa_holder_tracker.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import visa_holder_tracker.entity.VisaStatus;

import java.time.LocalDateTime;


/**
 * DTO request object used for creating
 * and updating visa holder records.
 *
 * <p>
 * Contains personal details, visa information,
 * travel entry details, and visa status.
 * </p>
 */
@Getter
@Setter
public class VisaHolderRequest {


    /**
     * Full legal name of the visa holder.
     */
    @NotBlank
    private String fullName;

    /**
     * Nationality of the visa holder.
     */
    @NotBlank
    private String nationality;

    /**
     * Passport number associated with the visa holder.
     */
    @NotBlank
    private String passportNumber;


    /**
     * Visa category or visa type.
     */
    @NotBlank
    private String visaType;


    /**
     * Visa expiry date and time.
     *
     * <p>
     * Must be a future date.
     * </p>
     */
    @Future
    private LocalDateTime expiryDate;


    /**
     * Date and time the visa holder entered the country.
     *
     * <p>
     * Must be a past or present date.
     * </p>
     */
    @PastOrPresent
    private LocalDateTime entryDate;

    /**
     * Current visa status of the visa holder.
     */
    @NotNull
    private VisaStatus status;
}
