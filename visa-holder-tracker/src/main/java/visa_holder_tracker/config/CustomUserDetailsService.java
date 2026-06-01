package visa_holder_tracker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.repository.AdminRepository;


/**
 * Custom implementation of Spring Security's {@link UserDetailsService}
 * used for loading application users during authentication.
 *
 * <p>
 * This service:
 * <ul>
 *     <li>Searches for users in the users table.</li>
 *     <li>Falls back to the admins table if no user is found.</li>
 *     <li>Builds Spring Security {@link UserDetails} objects.</li>
 *     <li>Provides user credentials and roles to Spring Security.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{


    /**
     * Repository used to retrieve regular application users.
     */
    private final UserRepository userRepository;

    /**
     * Repository used to retrieve administrator users.
     */
    private final AdminRepository adminRepository;


    /**
     * Loads a user by username for authentication.
     *
     * <p>
     * The method first searches the users table.
     * If no matching user is found, it searches the admins table.
     * </p>
     *
     * <p>
     * A Spring Security {@link UserDetails} object is created
     * containing:
     * <ul>
     *     <li>Username</li>
     *     <li>Encrypted password</li>
     *     <li>User roles and authorities</li>
     * </ul>
     * </p>
     *
     * @param username the username used during login
     * @return authenticated user's {@link UserDetails}
     * @throws UsernameNotFoundException if no matching user exists
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check users table first
        return userRepository.findByFullName(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getFullName())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name().replace("ROLE_", ""))
                        .build())
                // Fall back to admins table
                .or(() -> adminRepository.findByFullName(username)
                        .map(admin -> org.springframework.security.core.userdetails.User
                                .withUsername(admin.getFullName())
                                .password(admin.getPasswordHash())
                                .roles(admin.getRole().name().replace("ROLE_", ""))
                                .build()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}

