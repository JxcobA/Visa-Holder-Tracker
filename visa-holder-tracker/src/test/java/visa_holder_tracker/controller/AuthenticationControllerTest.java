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
import visa_holder_tracker.jwt_utils.GenerateJWTToken;
import visa_holder_tracker.jwt_utils.JwtFilter;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.UserRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({LoginSecurityConfig.class, CustomUserDetailsService.class, JwtFilter.class, GenerateJWTToken.class})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // CustomUserDetailsService queries these user and admin repos to load users
    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AdminRepository adminRepository;


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

    @Test
    void login_validUserCredentials_returnsJwtToken() throws Exception {
        String bcryptPassword = "$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy";

        User user = User.builder().passportNumber("P654321").fullName("Normal User").passwordHash(bcryptPassword).email("user1@dreamteam.com").role(Role.USER).build();

        when(userRepository.findByFullName("Normal User")).thenReturn(Optional.of(user));

        LoginRequest body = new LoginRequest("Normal User", "password");
        // Checks security processes work correctly with valid credentials
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(objectMapper.writeValueAsString(body))).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returnsunauthorised() throws Exception {
        String bcryptPassword = "$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy";

        Admin admin = Admin.builder().fullName("Admin User").passwordHash(bcryptPassword).email("admin@dreamteam.com").role(Role.ADMIN).build();

        when(userRepository.findByFullName("Admin User")).thenReturn(Optional.empty());
        when(adminRepository.findByFullName("Admin User")).thenReturn(Optional.of(admin));

        // "wrongpassword" doesn't match the BCrypt hash so Spring Security throws BadCredentialsException,
        // This maps to 401 unauthorised by default
        LoginRequest body = new LoginRequest("Admin User", "wrongpassword");

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(objectMapper.writeValueAsString(body))).andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownUser_returnsUnauthorised() throws Exception {
        // Both repos return empty so UsernameNotFoundException is thrown, maps to 401
        when(userRepository.findByFullName("Ghost")).thenReturn(Optional.empty());
        when(adminRepository.findByFullName("Ghost")).thenReturn(Optional.empty());

        LoginRequest body = new LoginRequest("Ghost", "password");

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(objectMapper.writeValueAsString(body))).andExpect(status().isUnauthorized());
    }
}