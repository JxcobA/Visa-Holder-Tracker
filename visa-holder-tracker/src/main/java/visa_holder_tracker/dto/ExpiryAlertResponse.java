package visa_holder_tracker.dto;

import visa_holder_tracker.entity.VisaHolder;

import java.util.List;

public record ExpiryAlertResponse(
        List<VisaHolder> expiringSoon,
        List<VisaHolder> expired
) {}
