package visa_holder_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


/**
 * Entity representing a standard application user.
 *
 * <p>
 * Users are authenticated through Spring Security
 * and are assigned application roles that determine
 * their access permissions.
 * </p>
 *
 * <p>
 * This entity stores:
 * <ul>
 *     <li>User identification details</li>
 *     <li>Authentication credentials</li>
 *     <li>Authorization roles</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {


    /**
     * Passport number used as the unique identifier
     * for the user.
     */
    @Id
    @Column(nullable = false, unique = true)
    private String passportNumber;


    /**
     * Security role assigned to the user.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    /**
     * Full name used as the username for authentication.
     *
     * <p>
     * Must be unique within the system.
     * </p>
     */
    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true)
    private String fullName;


    /**
     * Encrypted password hash used for authentication.
     */
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String passwordHash;


    /**
     * User email address.
     *
     * <p>
     * Must be unique within the system.
     * </p>
     */
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

}
