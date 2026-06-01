package visa_holder_tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import visa_holder_tracker.entity.Movement;
import java.time.LocalDateTime;


/**
 * DTO response object used for returning
 * visa holder movement information.
 *
 * @param id movement record identifier
 * @param entryDate visa holder entry date and time
 * @param exitDate visa holder exit date and time
 * @param passportNumber associated visa holder passport number
 */
public record MovementResponse(
        Long id,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime entryDate,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime exitDate,
        String passportNumber) {


    /**
     * Converts a {@link Movement} entity
     * into a {@link MovementResponse} DTO.
     *
     * @param movement movement entity
     * @return mapped movement response DTO
     */
    public static MovementResponse from(Movement movement) {
        return new MovementResponse(movement.getId(), movement.getEntryDate(),
                movement.getExitDate(), movement.getVisaHolder().getPassportNumber());
    }
}