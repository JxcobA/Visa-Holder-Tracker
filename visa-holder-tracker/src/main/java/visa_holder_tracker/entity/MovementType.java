package visa_holder_tracker.entity;

/**
 * Enum representing the type of visa holder movement event.
 *
 * <p>
 * Used to identify whether a visa holder:
 * <ul>
 *     <li>Entered the country</li>
 *     <li>Exited the country</li>
 * </ul>
 * </p>
 *
 * <p>
 * This enum is primarily used within:
 * <ul>
 *     <li>{@code MovementService}</li>
 *     <li>{@code MovementRequest}</li>
 *     <li>{@code Movement} entity processing</li>
 * </ul>
 * </p>
 */

// This enum is used in MovementService and MovementRequest
// Movement entity has a separate variable definition
public enum MovementType {
    ENTRY,
    EXIT
}
