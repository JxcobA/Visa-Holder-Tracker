package visa_holder_tracker.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import visa_holder_tracker.entity.VisaStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class VisaHolderRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String nationality;

    @NotBlank
    private String passportNumber;

    @NotBlank
    private String visaType;

    @Future
    private LocalDateTime expiryDate;

    @PastOrPresent
    private LocalDateTime entryDate;

    @NotNull
    private VisaStatus status;
}
