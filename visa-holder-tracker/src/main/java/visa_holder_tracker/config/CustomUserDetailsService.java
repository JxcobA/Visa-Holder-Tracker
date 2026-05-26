package visa_holder_tracker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.repository.AdminRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check users table first
        return userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getFullName())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name().replace("ROLE_", ""))
                        .build())
                // Fall back to admins table
                .or(() -> adminRepository.findByUsername(username)
                        .map(admin -> org.springframework.security.core.userdetails.User
                                .withUsername(admin.getFullName())
                                .password(admin.getPasswordHash())
                                .roles(admin.getRole().name().replace("ROLE_", ""))
                                .build()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}

