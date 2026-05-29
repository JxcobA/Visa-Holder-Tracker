package visa_holder_tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.Optional;


@Entity
@Getter
@Setter
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime entryDate;

    private LocalDateTime exitDate;

    @ManyToOne
    @JoinColumn(
            name = "passport_number",
            referencedColumnName = "passportNumber",
            nullable = false
    )
    private VisaHolder visaHolder;
}
