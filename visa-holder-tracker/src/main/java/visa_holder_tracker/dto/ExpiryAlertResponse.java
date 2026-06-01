package visa_holder_tracker.dto;

import visa_holder_tracker.entity.VisaHolder;

import java.util.List;


/**
 * DTO response object used for returning
 * visa expiry alert information.
 *
 * <p>
 * Contains:
 * <ul>
 *     <li>Visa holders whose visas are expiring soon.</li>
 *     <li>Visa holders whose visas have already expired.</li>
 * </ul>
 * </p>
 *
 * @param expiringSoon list of visa holders approaching expiry
 * @param expired list of visa holders with expired visas
 */
public record ExpiryAlertResponse(
        List<VisaHolder> expiringSoon,
        List<VisaHolder> expired
) {}
