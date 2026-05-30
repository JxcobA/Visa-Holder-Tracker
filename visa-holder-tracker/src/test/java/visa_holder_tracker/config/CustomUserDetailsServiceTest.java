package visa_holder_tracker.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import visa_holder_tracker.entity.Admin;
import visa_holder_tracker.entity.Role;
import visa_holder_tracker.entity.User;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User regularUser;
    private Admin adminUser;

    @BeforeEach
    void setUp() {
        regularUser = User.builder().passportNumber("P654321").fullName("Normal User").passwordHash("$2a$12$hashedpassword").email("user@test.com").role(Role.USER).build();

        adminUser = Admin.builder().fullName("Admin User").passwordHash("$2a$12$hashedpassword").email("admin@test.com").role(Role.ADMIN).build();
    }


    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        when(userRepository.findByFullName("Normal User")).thenReturn(Optional.of(regularUser));

        UserDetails result = userDetailsService.loadUserByUsername("Normal User");
        // Checks username matches what is expected
        assertThat(result.getUsername()).isEqualTo("Normal User");
        assertThat(result.getPassword()).isEqualTo("$2a$12$hashedpassword");
    }

    @Test
    void loadUserByUsername_existingUser_hasRoleUser() {
        when(userRepository.findByFullName("Normal User")).thenReturn(Optional.of(regularUser));

        UserDetails result = userDetailsService.loadUserByUsername("Normal User");
        // Checks that the role matches what is expected
        assertThat(result.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }


    @Test
    void loadUserByUsername_admin_hasRoleAdmin() {
        when(userRepository.findByFullName("Admin User")).thenReturn(Optional.empty());
        when(adminRepository.findByFullName("Admin User")).thenReturn(Optional.of(adminUser));

        UserDetails result = userDetailsService.loadUserByUsername("Admin User");
        // Checks that the role matches what is expected
        assertThat(result.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    void loadUserByUsername_notInTables_throwsUsernameNotFoundException() {
        when(userRepository.findByFullName("Ghost")).thenReturn(Optional.empty());
        when(adminRepository.findByFullName("Ghost")).thenReturn(Optional.empty());

        // The service throw UsernameNotFoundException on this to map the failure to a 401 response
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("Ghost")).isInstanceOf(UsernameNotFoundException.class).hasMessageContaining("Ghost");
    }

    @Test
    void loadUserByUsername_userFoundFirst_adminTableNeverQueried() {
        when(userRepository.findByFullName("Normal User")).thenReturn(Optional.of(regularUser));

        userDetailsService.loadUserByUsername("Normal User");
        // Checks fallback system works as intended
        verify(adminRepository, never()).findByFullName(any());
    }
}