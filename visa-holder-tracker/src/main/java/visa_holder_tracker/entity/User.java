package visa_holder_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(nullable = false, unique = true)
    private String passportNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true)
    private String fullName;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String passwordHash;

    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

}
