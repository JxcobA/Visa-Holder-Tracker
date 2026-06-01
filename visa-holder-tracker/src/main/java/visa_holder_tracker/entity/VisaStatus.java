package visa_holder_tracker.entity;


/**
 * Enum representing the current visa status
 * of a visa holder.
 *
 * <p>
 * Used throughout the application for:
 * <ul>
 *     <li>Visa filtering</li>
 *     <li>Compliance monitoring</li>
 *     <li>Expiry tracking</li>
 *     <li>Status reporting</li>
 * </ul>
 * </p>
 */
public enum VisaStatus {

    /**
     * Indicates the visa is currently valid and active.
     */
    ACTIVE,

    /**
     * Indicates the visa validity period has expired.
     */
    EXPIRED
    // Could add more values like EXPIRING_SOON or OVERSTAY for filtering and compliance
}
