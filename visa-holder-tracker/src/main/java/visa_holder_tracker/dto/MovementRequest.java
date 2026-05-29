package visa_holder_tracker.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import visa_holder_tracker.entity.MovementType;

import java.time.LocalDateTime;


@Getter
@Setter
public class MovementRequest {
    @NotBlank
    private String passportNumber;

    @NotNull
    private MovementType type;

    @NotNull
    private LocalDateTime date;
}
