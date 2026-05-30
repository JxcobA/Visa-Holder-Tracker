package visa_holder_tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import visa_holder_tracker.entity.Role;
import visa_holder_tracker.entity.User;
import visa_holder_tracker.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Test
    void login_succeeds_withValidCredentials() throws Exception {
        // Arrange
        User user = User.builder()
                .passportNumber("A123")
                .role(Role.USER)
                .fullName("Newlife")
                .email("newlife@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);

        // Act + Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Newlife\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_fails_withWrongPassword() throws Exception {
        User user = User.builder()
                .passportNumber("A123")
                .role(Role.USER)
                .fullName("Newlife")
                .email("newlife@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Newlife\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isForbidden());
    }
}