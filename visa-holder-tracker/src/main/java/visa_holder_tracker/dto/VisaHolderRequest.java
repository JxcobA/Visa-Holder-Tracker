package visa_holder_tracker.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

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
    private LocalDate expiryDate;

    @PastOrPresent
    private LocalDate entryDate;
}
