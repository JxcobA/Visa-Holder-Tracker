package visa_holder_tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Optional;


@Entity
@Getter
@Setter
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate entryDate;

    private LocalDate exitDate;

    @ManyToOne
    @JoinColumn(
            name = "passport_number",
            referencedColumnName = "passportNumber",
            nullable = false
    )
    private VisaHolder visaHolder;
}
