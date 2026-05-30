package visa_holder_tracker.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import visa_holder_tracker.entity.*;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VisaHolderRepository visaHolderRepository;

    @Test
    void generateReport_countsActive_User() throws Exception {
        // 1. Seed one clearly ACTIVE holder (not expired, not expiring soon)
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("RPT1").fullName("Active Person")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(2))   // far future
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 2. Seed user + log in
        User user = User.builder()
                .passportNumber("U7").role(Role.USER).fullName("Reporter")
                .email("reporter@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        userRepository.save(user);
        String token = loginAndGetToken("Reporter", "password");

        // 3. Hit summary, expect the counts
        mockMvc.perform(get("/api/reports/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(1))
                .andExpect(jsonPath("$.expired").value(0))
                .andExpect(jsonPath("$.expiringSoon").value(0));
    }

    @Test
    void generateReport_countsActive_Admin() throws Exception {
        // 1. Seed one clearly ACTIVE holder (not expired, not expiring soon)
        visaHolderRepository.save(VisaHolder.builder()
                .passportNumber("RPT1").fullName("Active Person")
                .nationality("Testland").visaType("Work")
                .expiryDate(LocalDateTime.now().plusYears(2))   // far future
                .entryDate(LocalDateTime.now())
                .status(VisaStatus.ACTIVE).build());

        // 2. Seed user + log in
        Admin admin = Admin.builder()
                .role(Role.ADMIN).fullName("Luke")
                .email("luke@test.com")
                .passwordHash(new BCryptPasswordEncoder(12).encode("password"))
                .build();
        adminRepository.save(admin);
        String token = loginAndGetToken("Luke", "password");

        // 3. Hit summary, expect the counts
        mockMvc.perform(get("/api/reports/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(1))
                .andExpect(jsonPath("$.expired").value(0))
                .andExpect(jsonPath("$.expiringSoon").value(0));
    }

    @Test
    void downloadReport() {
    }

    @AfterEach
    void cleanUp() {
        visaHolderRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(response, "$.token");
    }
}