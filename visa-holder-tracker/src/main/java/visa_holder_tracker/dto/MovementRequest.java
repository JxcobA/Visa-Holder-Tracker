package visa_holder_tracker.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import visa_holder_tracker.entity.MovementType;

import java.time.LocalDateTime;


/**
 * DTO request object used for logging
 * visa holder movement events.
 *
 * <p>
 * Represents entry or exit activity
 * associated with a visa holder.
 * </p>
 */
@Getter
@Setter
public class MovementRequest {

    /**
     * Passport number of the visa holder.
     */
    @NotBlank
    private String passportNumber;

    /**
     * Type of movement event
     * such as ENTRY or EXIT.
     */
    @NotNull
    private MovementType type;


    /**
     * Date and time the movement occurred.
     */
    @NotNull
    private LocalDateTime date;
}
