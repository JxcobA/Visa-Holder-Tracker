package visa_holder_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "visa_holders")
// The following are lombrok annotations - reduces boilerplate code
@Getter // Generates getter at compile time
@Setter// Generates setter at compile time
@NoArgsConstructor// Generates no args constructor at compile time
@AllArgsConstructor// Generates all args constructor at compile time
@Builder // Generates a builder class for constructing objects
public class VisaHolder {

    @Id
    private String passportNumber;

    // This annotation is a bean validation - Rejects a field if it is null, an empty string or only whitespace
    // The message parameter is returned if validation fails.
    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Nationality is required")
    @Column(nullable = false)
    private String nationality;

    @NotBlank(message = "Visa type is required")
    @Column(nullable = false)
    private String visaType;

    @NotBlank(message = "Visa expiry date is required")
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @NotBlank(message = "Entry date is required")
    @Column(nullable = false)
    private LocalDateTime entryDate;


    @Enumerated(EnumType.STRING) // Determines how enum should be persisted in DB - as a String
    @Column(nullable = false)
    private VisaStatus status;

    @OneToMany(mappedBy = "visaHolder")
    private List<Movement> movements;

}
