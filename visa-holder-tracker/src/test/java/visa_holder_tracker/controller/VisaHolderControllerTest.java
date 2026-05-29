package visa_holder_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import visa_holder_tracker.authentication.LoginSecurityConfig;
import visa_holder_tracker.config.CustomUserDetailsService;
import visa_holder_tracker.dto.VisaHolderRequest;
import visa_holder_tracker.entity.VisaStatus;
import visa_holder_tracker.jwt_utils.GenerateJWTToken;
import visa_holder_tracker.jwt_utils.JwtFilter;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.service.VisaHolderService;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisaHolderController.class)
@Import({LoginSecurityConfig.class, CustomUserDetailsService.class, JwtFilter.class, GenerateJWTToken.class})
public class VisaHolderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    AdminRepository adminRepository;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    VisaHolderService visaHolderService;

    // Obtained via /api/auth/login in @BeforeEach
    private String adminToken;

    // Helper
    private VisaHolderRequest buildValidRequest() {
        VisaHolderRequest req = new VisaHolderRequest();
        req.setPassportNumber("PN123456");
        req.setFullName("Test Name");
        req.setNationality("British");
        req.setVisaType("Student");
        req.setStatus(VisaStatus.ACTIVE);
        req.setExpiryDate(LocalDateTime.now().plusYears(1));
        req.setEntryDate(LocalDateTime.now().minusDays(1));
        return req;
    }


    // Tests
    @Test
    @WithMockUser(roles = "ADMIN")
    void createVisaHolder_blankFullName_returns400() throws Exception {
        VisaHolderRequest bad = new VisaHolderRequest();
        bad.setPassportNumber("PN123");
        bad.setFullName("");             // @NotBlank violation
        bad.setNationality("British");
        bad.setVisaType("Student");
        bad.setStatus(VisaStatus.ACTIVE);
        bad.setExpiryDate(LocalDateTime.now().plusYears(1));
        bad.setEntryDate(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/visa-holders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createVisaHolder_expiryInPast_returns400() throws Exception {
        VisaHolderRequest bad = buildValidRequest();
        bad.setExpiryDate(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/visa-holders")
                        .contentType(MediaType.APPLICATION_JSON)  // no .header() line
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createVisaHolder_entryInFuture_returns400() throws Exception {
        VisaHolderRequest bad = buildValidRequest();
        bad.setEntryDate(LocalDateTime.now().plusDays(5));

        mockMvc.perform(post("/api/visa-holders")
                        .contentType(MediaType.APPLICATION_JSON)  // no .header() line
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllVisaHolders_returnsOk() throws Exception {
        mockMvc.perform(get("/api/visa-holders"))
                .andExpect(status().isOk());
    }
}
