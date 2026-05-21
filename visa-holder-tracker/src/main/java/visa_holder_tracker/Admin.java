package visa_holder_tracker;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
