package visa_holder_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


/**
 * Entity representing an administrator account
 * within the application.
 *
 * <p>
 * Admin accounts are used for authentication
 * and authorisation in the system and contain:
 * <ul>
 *     <li>Login credentials</li>
 *     <li>Email information</li>
 *     <li>Assigned security role</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    /**
     * Unique identifier for the administrator.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Security role assigned to the administrator.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    /**
     * Full name used as the administrator username.
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
     * Administrator email address.
     *
     * <p>
     * Must be unique within the system.
     * </p>
     */
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

}
