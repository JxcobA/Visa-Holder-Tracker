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


/**
 * Unit tests for {@link CustomUserDetailsService}.
 *
 * <p>
 * These tests verify:
 * <ul>
 *     <li>User lookup from the users table.</li>
 *     <li>Fallback lookup from the admins table.</li>
 *     <li>Correct role mapping for Spring Security.</li>
 *     <li>Exception handling when users are not found.</li>
 *     <li>Repository interaction behavior.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Uses Mockito for mocking repository dependencies
 * without loading the Spring application context.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    /**
     * Mocked repository for regular users.
     */
    @Mock
    private UserRepository userRepository;

    /**
     * Mocked repository for administrator users.
     */
    @Mock
    private AdminRepository adminRepository;

    /**
     * Service under test with mocked dependencies injected.
     */
    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    /**
     * Sample regular user used during tests.
     */
    private User regularUser;

    /**
     * Sample administrator user used during tests.
     */
    private Admin adminUser;


    /**
     * Initializes reusable test data before each test.
     *
     * <p>
     * Creates:
     * <ul>
     *     <li>A standard USER account.</li>
     *     <li>An ADMIN account.</li>
     * </ul>
     * </p>
     */
    @BeforeEach
    void setUp() {
        regularUser = User.builder().passportNumber("P654321").fullName("Normal User").passwordHash("$2a$12$hashedpassword").email("user@test.com").role(Role.USER).build();

        adminUser = Admin.builder().fullName("Admin User").passwordHash("$2a$12$hashedpassword").email("admin@test.com").role(Role.ADMIN).build();
    }


    /**
     * Verifies that an existing user
     * is successfully loaded from the users table.
     *
     * <p>
     * This test confirms:
     * <ul>
     *     <li>The correct username is returned.</li>
     *     <li>The stored password hash is preserved.</li>
     * </ul>
     * </p>
     */
    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        when(userRepository.findByFullName("Normal User")).thenReturn(Optional.of(regularUser));

        UserDetails result = userDetailsService.loadUserByUsername("Normal User");
        // Checks username matches what is expected
        assertThat(result.getUsername()).isEqualTo("Normal User");
        assertThat(result.getPassword()).isEqualTo("$2a$12$hashedpassword");
    }

    /**
     * Verifies that a regular user
     * receives the ROLE_USER authority.
     *
     * <p>
     * Ensures Spring Security role mapping
     * is correctly generated from the entity role.
     * </p>
     */
    @Test
    void loadUserByUsername_existingUser_hasRoleUser() {
        when(userRepository.findByFullName("Normal User")).thenReturn(Optional.of(regularUser));

        UserDetails result = userDetailsService.loadUserByUsername("Normal User");
        // Checks that the role matches what is expected
        assertThat(result.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }


    /**
     * Verifies that administrator users
     * receive the ROLE_ADMIN authority.
     *
     * <p>
     * This test also validates the fallback logic:
     * <ul>
     *     <li>User table lookup fails first.</li>
     *     <li>Admin table lookup succeeds afterward.</li>
     * </ul>
     * </p>
     */
    @Test
    void loadUserByUsername_admin_hasRoleAdmin() {
        when(userRepository.findByFullName("Admin User")).thenReturn(Optional.empty());
        when(adminRepository.findByFullName("Admin User")).thenReturn(Optional.of(adminUser));

        UserDetails result = userDetailsService.loadUserByUsername("Admin User");
        // Checks that the role matches what is expected
        assertThat(result.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Verifies that a {@link UsernameNotFoundException}
     * is thrown when the username does not exist
     * in either repository.
     *
     * <p>
     * Ensures authentication failures
     * are correctly propagated to Spring Security.
     * </p>
     */
    @Test
    void loadUserByUsername_notInTables_throwsUsernameNotFoundException() {
        when(userRepository.findByFullName("Ghost")).thenReturn(Optional.empty());
        when(adminRepository.findByFullName("Ghost")).thenReturn(Optional.empty());

        // The service throw UsernameNotFoundException on this to map the failure to a 401 response
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("Ghost")).isInstanceOf(UsernameNotFoundException.class).hasMessageContaining("Ghost");
    }

    /**
     * Verifies that the admin repository
     * is not queried when a user is already found
     * in the users table.
     *
     * <p>
     * Ensures the fallback lookup logic
     * behaves efficiently and correctly.
     * </p>
     */
    @Test
    void loadUserByUsername_userFoundFirst_adminTableNeverQueried() {
        when(userRepository.findByFullName("Normal User")).thenReturn(Optional.of(regularUser));

        userDetailsService.loadUserByUsername("Normal User");
        // Checks fallback system works as intended
        verify(adminRepository, never()).findByFullName(any());
    }
}