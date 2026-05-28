package visa_holder_tracker.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
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
