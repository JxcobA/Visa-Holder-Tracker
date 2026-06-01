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


/**
 * Web layer tests for {@link ReportController}.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>Report summary endpoint responses.</li>
 *     <li>Report download endpoint behavior.</li>
 *     <li>Spring Security authorization rules.</li>
 *     <li>Pre-signed URL response handling.</li>
 *     <li>Correct interaction with service layer dependencies.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Uses:
 * <ul>
 *     <li>{@link WebMvcTest} for lightweight controller testing.</li>
 *     <li>{@link MockMvc} for HTTP request simulation.</li>
 *     <li>Mockito service mocking.</li>
 *     <li>{@link WithMockUser} for authenticated security testing.</li>
 * </ul>
 * </p>
 */
@WebMvcTest(ReportController.class)
@Import({LoginSecurityConfig.class, CustomUserDetailsService.class, JwtFilter.class, GenerateJwtToken.class})
class ReportControllerTest {


    /**
     * Mock MVC client used to simulate HTTP requests.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mocked notification service required
     * for Spring context initialization.
     */
    @MockitoBean
    SqsNotificationService sqsNotificationService;

    /**
     * Mocked report service used
     * to isolate controller behavior.
     *
     * <p>
     * Required for wiring {@link ReportController}.
     * </p>
     */
    // Required so Spring can wire ReportController
    @MockitoBean
    private ReportService reportService;

    /**
     * Mocked visa holder service used
     * for report summary calculations.
     */
    @MockitoBean
    private VisaHolderService visaHolderService;

    /**
     * Mocked user repository required
     * for Spring Security authentication setup.
     *
     * <p>
     * Used by {@link CustomUserDetailsService}.
     * </p>
     */
    // Required by CustomUserDetailsService as Spring still builds the full security context which wires these beans
    @MockitoBean
    private UserRepository userRepository;

    /**
     * Mocked admin repository required
     * for Spring Security authentication setup.
     */
    @MockitoBean
    private AdminRepository adminRepository;


    /**
     * Verifies that the report summary endpoint
     * returns all expected visa statistics.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Correct HTTP 200 response.</li>
     *     <li>Summary count calculation.</li>
     *     <li>JSON response structure.</li>
     *     <li>Correct mapping of report values.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @WithMockUser(roles = "USER")
    void generateReport_returnsAllFourSummaryCounts() throws Exception {
        when(visaHolderService.countActive()).thenReturn(5L);
        when(visaHolderService.getExpired()).thenReturn(List.of(new VisaHolder()));
        when(visaHolderService.getOverstayed()).thenReturn(List.of(new VisaHolder(), new VisaHolder()));
        when(visaHolderService.getExpiringSoon(30)).thenReturn(List.of(new VisaHolder()));

        mockMvc.perform(get("/api/reports/summary")).andExpect(status().isOk()).andExpect(jsonPath("$.active").value(5)).andExpect(jsonPath("$.expired").value(1)).andExpect(jsonPath("$.overstay").value(2)).andExpect(jsonPath("$.expiringSoon").value(1));
    }


    /**
     * Verifies that administrators are also authorized
     * to access the report summary endpoint.
     *
     * <p>
     * This test validates role-based access control
     * configured with:
     * {@code hasAnyRole('USER', 'ADMIN')}.
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void generateReport_adminRole_isAlsoPermitted() throws Exception {
        when(visaHolderService.countActive()).thenReturn(0L);
        when(visaHolderService.getExpired()).thenReturn(List.of());
        when(visaHolderService.getOverstayed()).thenReturn(List.of());
        when(visaHolderService.getExpiringSoon(30)).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/summary")).andExpect(status().isOk());
    }

    /**
     * Verifies that unauthenticated users
     * cannot access the report summary endpoint.
     *
     * <p>
     * This test validates Spring Security
     * authentication enforcement by ensuring
     * requests without authentication
     * return HTTP 401 Unauthorized.
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    void generateReport_unauthenticated_returnsUnauthorised() throws Exception {
        // Simulates a request with no JWT token at all
        mockMvc.perform(get("/api/reports/summary")).andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that authenticated users
     * can successfully download reports.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Successful report generation flow.</li>
     *     <li>HTTP 200 response handling.</li>
     *     <li>Controller integration with {@link ReportService}.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @WithMockUser(roles = "USER")
    void downloadReport_withDate_returnsOk() throws Exception {
        when(reportService.generateAndUpload("2026-05")).thenReturn("https://fake-url.com");
        mockMvc.perform(get("/api/reports/download/2026-05")).andExpect(status().isOk());
    }

    /**
     * Verifies that unauthenticated users
     * cannot access the report download endpoint.
     *
     * <p>
     * Ensures protected endpoints correctly enforce
     * authentication requirements.
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    void downloadReport_unauthenticated_returnsUnauthorised() throws Exception {
        mockMvc.perform(get("/api/reports/download/2026-05")).andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that the report download endpoint
     * returns the generated pre-signed URL
     * in the JSON response body.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Correct JSON serialization.</li>
     *     <li>Pre-signed URL response structure.</li>
     *     <li>Successful report service integration.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @WithMockUser(roles = "USER")
    void downloadReport_returnsPresignedUrlInBody() throws Exception {
        when(reportService.generateAndUpload("2026-05")).thenReturn("https://fake-url.com");

        mockMvc.perform(get("/api/reports/download/2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://fake-url.com"));
    }

    /**
     * Verifies that the controller passes
     * the correct date parameter
     * to the report service.
     *
     * <p>
     * Ensures request path variables
     * are correctly forwarded to
     * service layer methods.
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @WithMockUser(roles = "USER")
    void downloadReport_callsServiceWithCorrectDate() throws Exception {
        when(reportService.generateAndUpload("2025-11")).thenReturn("https://fake-url.com");

        mockMvc.perform(get("/api/reports/download/2025-11"))
                .andExpect(status().isOk());

        verify(reportService).generateAndUpload("2025-11");
    }


}