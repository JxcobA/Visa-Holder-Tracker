package visa_holder_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import visa_holder_tracker.entity.Movement;
import java.time.LocalDateTime;

public record MovementResponse(
        Long id,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime entryDate,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime exitDate,
        String passportNumber) {

    public static MovementResponse from(Movement movement) {
        return new MovementResponse(movement.getId(), movement.getEntryDate(),
                movement.getExitDate(), movement.getVisaHolder().getPassportNumber());
    }
}