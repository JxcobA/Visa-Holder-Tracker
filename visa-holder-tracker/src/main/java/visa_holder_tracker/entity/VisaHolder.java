package visa_holder_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Entity representing a visa holder within the system.
 *
 * <p>
 * This entity stores:
 * <ul>
 *     <li>Personal identification details</li>
 *     <li>Visa information</li>
 *     <li>Entry and expiry dates</li>
 *     <li>Current visa status</li>
 *     <li>Movement history records</li>
 * </ul>
 * </p>
 *
 * <p>
 * Each visa holder is uniquely identified
 * by their passport number.
 * </p>
 */
@Entity
@Table(name = "visa_holders")
// The following are lombrok annotations - reduces boilerplate code
// The following are lombok annotations - reduces boilerplate code
@Getter // Generates getter at compile time
@Setter// Generates setter at compile time
@NoArgsConstructor// Generates no args constructor at compile time
@AllArgsConstructor// Generates all args constructor at compile time
@Builder // Generates a builder class for constructing objects
public class VisaHolder {


    /**
     * Unique passport number identifying the visa holder.
     */
    @Id
    private String passportNumber;


    /**
     * Full legal name of the visa holder.
     */
    // This annotation is a bean validation - Rejects a field if it is null, an empty string or only whitespace
    // The message parameter is returned if validation fails.
    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;


    /**
     * Nationality of the visa holder.
     */
    @NotBlank(message = "Nationality is required")
    @Column(nullable = false)
    private String nationality;


    /**
     * Visa category assigned to the visa holder.
     */
    @NotBlank(message = "Visa type is required")
    @Column(nullable = false)
    private String visaType;


    /**
     * Visa expiration date and time.
     */

    // Datatype mismatch LocalDateTime used here but
    // getExpiringSoon and getExpired methods are LocalDate, is just inconsistent, I recommend LocalDateTime, LocalDate gave me issues.
    // but do check for errors
    @NotNull(message = "Visa expiry date is required")
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Date and time the visa holder entered the country.
     */
    @NotNull(message = "Entry date is required")
    @Column(nullable = false)
    private LocalDateTime entryDate;


    /**
     * Current status of the visa holder.
     */
    @Enumerated(EnumType.STRING) // Determines how enum should be persisted in DB - as a String
    @Column(nullable = false)
    private VisaStatus status;


    /**
     * List of movement records associated
     * with the visa holder.
     *
     * <p>
     * Includes entry and exit history.
     * </p>
     */
    @OneToMany(mappedBy = "visaHolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Movement> movements;

}
