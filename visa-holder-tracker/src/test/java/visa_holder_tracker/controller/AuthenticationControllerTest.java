package visa_holder_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import visa_holder_tracker.authentication.LoginSecurityConfig;
import visa_holder_tracker.config.CustomUserDetailsService;
import visa_holder_tracker.controller.AuthenticationController.LoginRequest;
import visa_holder_tracker.entity.Role;
import visa_holder_tracker.entity.User;
import visa_holder_tracker.entity.Admin;
import visa_holder_tracker.jwt_utils.GenerateJwtToken;
import visa_holder_tracker.jwt_utils.JwtFilter;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.UserRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Web layer integration tests for {@link AuthenticationController}.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>User and admin authentication flows.</li>
 *     <li>JWT token generation after successful login.</li>
 *     <li>Spring Security authentication behavior.</li>
 *     <li>Unauthorized responses for invalid credentials.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Uses:
 * <ul>
 *     <li>{@link WebMvcTest} for lightweight controller testing.</li>
 *     <li>{@link MockMvc} for HTTP request simulation.</li>
 *     <li>Mockito repositories for controlled authentication scenarios.</li>
 * </ul>
 * </p>
 */
@WebMvcTest(AuthenticationController.class)
@Import({LoginSecurityConfig.class, CustomUserDetailsService.class, JwtFilter.class, GenerateJwtToken.class})
class AuthenticationControllerTest {

    /**
     * Mock MVC client used to simulate HTTP requests
     * against the authentication API.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Jackson object mapper used
     * for JSON serialization.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Mocked repository used for loading regular users
     * during authentication tests.
     *
     * <p>
     * Queried by {@link CustomUserDetailsService}.
     * </p>
     */
    // CustomUserDetailsService queries these user and admin repos to load users
    @MockitoBean
    private UserRepository userRepository;


    /**
     * Mocked repository used for loading administrators
     * during authentication tests.
     */
    @MockitoBean
    private AdminRepository adminRepository;

    /**
     * Verifies that valid administrator credentials
     * successfully authenticate and return a JWT token.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Fallback lookup from the admin repository.</li>
     *     <li>Password verification using BCrypt.</li>
     *     <li>JWT token generation after authentication.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    void login_validAdminCredentials_returnsJwtToken() throws Exception {
        // Creates a real Spring Security UserDetails object with a BCrypt-hashed password
        String bcryptPassword = "$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy";

        Admin admin = Admin.builder().fullName("Admin User").passwordHash(bcryptPassword).email("admin@dreamteam.com").role(Role.ADMIN).build();

        // There is no regular user, so CustomUserDetailsService falls through to adminRepository
        when(userRepository.findByFullName("Admin User")).thenReturn(Optional.empty());
        when(adminRepository.findByFullName("Admin User")).thenReturn(Optional.of(admin));

        LoginRequest body = new LoginRequest("Admin User", "password");

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(objectMapper.writeValueAsString(body))).andExpect(status().isOk()).andExpect(jsonPath("$.token").isString()).andExpect(jsonPath("$.token").isNotEmpty());
    }


    /**
     * Verifies that valid regular user credentials
     * successfully authenticate and return a JWT token.
     *
     * <p>
     * Ensures:
     * <ul>
     *     <li>User lookup succeeds.</li>
     *     <li>Spring Security authentication passes.</li>
     *     <li>A JWT token is returned in the response.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    void login_validUserCredentials_returnsJwtToken() throws Exception {
        String bcryptPassword = "$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy";

        User user = User.builder().passportNumber("P654321").fullName("Normal User").passwordHash(bcryptPassword).email("user1@dreamteam.com").role(Role.USER).build();

        when(userRepository.findByFullName("Normal User")).thenReturn(Optional.of(user));

        LoginRequest body = new LoginRequest("Normal User", "password");
        // Checks security processes work correctly with valid credentials
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(objectMapper.writeValueAsString(body))).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
    }


    /**
     * Verifies that invalid passwords
     * result in HTTP 401 Unauthorized responses.
     *
     * <p>
     * This test confirms:
     * <ul>
     *     <li>BCrypt password validation is enforced.</li>
     *     <li>Incorrect passwords fail authentication.</li>
     *     <li>Spring Security maps failures to HTTP 401.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    void login_wrongPassword_returnsunauthorised() throws Exception {
        String bcryptPassword = "$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy";

        Admin admin = Admin.builder().fullName("Admin User").passwordHash(bcryptPassword).email("admin@dreamteam.com").role(Role.ADMIN).build();

        when(userRepository.findByFullName("Admin User")).thenReturn(Optional.empty());
        when(adminRepository.findByFullName("Admin User")).thenReturn(Optional.of(admin));

        // "wrongpassword" doesn't match the BCrypt hash so Spring Security throws BadCredentialsException,

        // Invalid credentials trigger authentication failure,
        // which is mapped to HTTP 401 Unauthorized
        LoginRequest body = new LoginRequest("Admin User", "wrongpassword");

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(objectMapper.writeValueAsString(body))).andExpect(status().isUnauthorized());
    }


    /**
     * Verifies that unknown usernames
     * return HTTP 401 Unauthorized responses.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>User lookup failure handling.</li>
     *     <li>Fallback admin lookup failure handling.</li>
     *     <li>Authentication rejection for unknown users.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    void login_unknownUser_returnsUnauthorised() throws Exception {
        // Authentication fails because both repositories return empty results,
        // so the login request is rejected with HTTP 401 Unauthorized
        when(userRepository.findByFullName("Ghost")).thenReturn(Optional.empty());
        when(adminRepository.findByFullName("Ghost")).thenReturn(Optional.empty());

        LoginRequest body = new LoginRequest("Ghost", "password");

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}