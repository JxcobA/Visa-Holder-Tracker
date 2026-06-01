package visa_holder_tracker.entity;


/**
 * Enum representing application security roles.
 *
 * <p>
 * Roles are used by Spring Security for:
 * <ul>
 *     <li>Authentication</li>
 *     <li>Authorization</li>
 *     <li>Endpoint access control</li>
 *     <li>Method-level security</li>
 * </ul>
 * </p>
 *
 * <p>
 * These roles are commonly referenced using
 * {@code @PreAuthorize} annotations.
 * </p>
 */
public enum Role {
    ADMIN,
    USER
}
