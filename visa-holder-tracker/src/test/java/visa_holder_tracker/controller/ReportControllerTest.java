package visa_holder_tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import visa_holder_tracker.authentication.LoginSecurityConfig;
import visa_holder_tracker.config.CustomUserDetailsService;
import visa_holder_tracker.entity.VisaHolder;
import visa_holder_tracker.jwt_utils.GenerateJwtToken;
import visa_holder_tracker.jwt_utils.JwtFilter;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.service.ReportService;
import visa_holder_tracker.service.SqsNotificationService;
import visa_holder_tracker.service.VisaHolderService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import({LoginSecurityConfig.class, CustomUserDetailsService.class, JwtFilter.class, GenerateJwtToken.class})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    SqsNotificationService sqsNotificationService;

    // Required so Spring can wire ReportController
    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private VisaHolderService visaHolderService;

    // Required by CustomUserDetailsService as Spring still builds the full security context which wires these beans
    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AdminRepository adminRepository;


    @Test
    @WithMockUser(roles = "USER")
    void generateReport_returnsAllFourSummaryCounts() throws Exception {
        when(visaHolderService.countActive()).thenReturn(5L);
        when(visaHolderService.getExpired()).thenReturn(List.of(new VisaHolder()));
        when(visaHolderService.getOverstayed()).thenReturn(List.of(new VisaHolder(), new VisaHolder()));
        when(visaHolderService.getExpiringSoon(30)).thenReturn(List.of(new VisaHolder()));

        mockMvc.perform(get("/api/reports/summary")).andExpect(status().isOk()).andExpect(jsonPath("$.active").value(5)).andExpect(jsonPath("$.expired").value(1)).andExpect(jsonPath("$.overstay").value(2)).andExpect(jsonPath("$.expiringSoon").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generateReport_adminRole_isAlsoPermitted() throws Exception {
        when(visaHolderService.countActive()).thenReturn(0L);
        when(visaHolderService.getExpired()).thenReturn(List.of());
        when(visaHolderService.getOverstayed()).thenReturn(List.of());
        when(visaHolderService.getExpiringSoon(30)).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/summary")).andExpect(status().isOk());
    }

    @Test
    void generateReport_unauthenticated_returnsUnauthorised() throws Exception {
        // Simulates a request with no JWT token at all
        mockMvc.perform(get("/api/reports/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void downloadReport_withDate_returnsOk() throws Exception {
        when(reportService.generateAndUpload("2026-05")).thenReturn("https://fake-url.com");
        mockMvc.perform(get("/api/reports/download/2026-05")).andExpect(status().isOk());
    }
    @Test
    void downloadReport_unauthenticated_returnsUnauthorised() throws Exception {
        mockMvc.perform(get("/api/reports/download/2026-05")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void downloadReport_returnsPresignedUrlInBody() throws Exception {
        when(reportService.generateAndUpload("2026-05")).thenReturn("https://fake-url.com");

        mockMvc.perform(get("/api/reports/download/2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://fake-url.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void downloadReport_callsServiceWithCorrectDate() throws Exception {
        when(reportService.generateAndUpload("2025-11")).thenReturn("https://fake-url.com");

        mockMvc.perform(get("/api/reports/download/2025-11"))
                .andExpect(status().isOk());

        verify(reportService).generateAndUpload("2025-11");
    }


}