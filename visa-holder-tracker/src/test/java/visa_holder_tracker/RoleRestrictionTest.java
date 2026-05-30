package visa_holder_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import visa_holder_tracker.entity.*;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleRestrictionTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    VisaHolderRepository visaHolderRepository;

    @Test
    void userCannotDeleteVisaHolder() throws Exception {
        // Arrange: put a USER in the DB
        User user = User.builder()
                .passportNumber("U1")
                .role(Role.USER)
                .fullName("Bob")
                .email("bob@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);

        // log in as that user to get a token
        String token = loginAndGetToken("Bob", "password");

        // Act + Assert: try to delete, expect 403 Forbidden
        mockMvc.perform(delete("/api/visa-holders/ANY123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanDeleteVisaHolder() throws Exception {
        // 1. Seed an ADMIN so we can log in as one
        Admin admin = Admin.builder()
                .role(Role.ADMIN)
                .fullName("Boby")
                .email("boby@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        adminRepository.save(admin);

        // 2. Seed a VISA HOLDER — this is the thing we'll delete
        VisaHolder holder = VisaHolder.builder()
                .passportNumber("DEL123")
                .fullName("ToDelete")
                .nationality("Testland")
                .visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE)
                .build();
        visaHolderRepository.save(holder);

        // 3. Log in as the admin to get a token
        String token = loginAndGetToken("Boby", "password");

        // 4. Delete that holder's passport, expect 204 No Content
        mockMvc.perform(delete("/api/visa-holders/DEL123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(response, "$.token");
    }

}
