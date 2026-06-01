package visa_holder_tracker.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.Optional;


/**
 * Entity representing a visa holder movement record.
 *
 * <p>
 * A movement record tracks entry and exit activity
 * associated with a visa holder.
 * </p>
 *
 * <p>
 * Each movement is linked to a specific visa holder
 * through the passport number relationship.
 * </p>
 */
@Entity
@Getter
@Setter
public class Movement {


    /**
     * Unique identifier for the movement record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Date and time the visa holder entered.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime entryDate;


    /**
     * Date and time the visa holder exited.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime exitDate;


    /**
     * Visa holder associated with this movement record.
     *
     * <p>
     * Linked using the visa holder passport number.
     * </p>
     */
    @ManyToOne
    @JoinColumn(
            name = "passport_number",
            referencedColumnName = "passportNumber",
            nullable = false
    )
    @JsonIgnore
    private VisaHolder visaHolder;
}
