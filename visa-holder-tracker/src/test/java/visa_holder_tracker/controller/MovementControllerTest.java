package visa_holder_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import visa_holder_tracker.authentication.LoginSecurityConfig;
import visa_holder_tracker.config.CustomUserDetailsService;
import visa_holder_tracker.dto.MovementRequest;
import visa_holder_tracker.dto.MovementResponse;
import visa_holder_tracker.entity.Movement;
import visa_holder_tracker.entity.MovementType;
import visa_holder_tracker.jwt_utils.GenerateJwtToken;
import visa_holder_tracker.jwt_utils.JwtFilter;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.service.MovementService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Web layer tests for {@link MovementController}.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>Movement creation endpoints.</li>
 *     <li>Movement retrieval endpoints.</li>
 *     <li>Spring Security authorization rules.</li>
 *     <li>HTTP response status and payload correctness.</li>
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
@WebMvcTest(MovementController.class)
@Import({LoginSecurityConfig.class, CustomUserDetailsService.class, JwtFilter.class, GenerateJwtToken.class})
class MovementControllerTest {


    /**
     * Mock MVC client used to simulate HTTP requests.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Jackson object mapper used for JSON serialization.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Mocked movement service used
     * to isolate controller behavior.
     */
    @MockitoBean
    private MovementService movementService;

    /**
     * Mocked user repository required
     * for Spring Security authentication setup.
     */
    @MockitoBean
    private UserRepository userRepository;

    /**
     * Mocked admin repository required
     * for Spring Security authentication setup.
     */
    @MockitoBean
    private AdminRepository adminRepository;

//    @MockitoBean
//    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    /**
     * Verifies that administrators can successfully
     * create movement records.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Authenticated ADMIN access.</li>
     *     <li>Movement request handling.</li>
     *     <li>HTTP 201 Created responses.</li>
     *     <li>Correct JSON response serialization.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void logMovement_shouldReturnCreatedMovement() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setPassportNumber("A1234567");
        request.setType(MovementType.ENTRY);
        request.setDate(LocalDateTime.of(2026, 5, 29,00,00));

        Movement savedMovement = new Movement();
        savedMovement.setEntryDate(LocalDateTime.of(2026, 5, 29,00,00));

        when(movementService.logMovement(any(MovementRequest.class)))
                .thenReturn(savedMovement);

        mockMvc.perform(post("/api/movements")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entryDate").value("2026-05-29T00:00:00"));
    }

    /**
     * Verifies that authenticated users
     * can retrieve visa holder movement history.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Movement retrieval endpoint behavior.</li>
     *     <li>Correct JSON array responses.</li>
     *     <li>Movement response serialization.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getMovements_shouldReturnMovementList() throws Exception {
        MovementResponse response = new MovementResponse(
                1L, LocalDateTime.of(2026, 5, 29, 0, 0), null, "A1234567");

        when(movementService.getMovementsByHolderId("A1234567"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/movements/A1234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entryDate").value("2026-05-29T00:00:00"));
    }

    /**
     * Verifies that unauthenticated requests
     * to protected movement endpoints
     * are rejected with HTTP 401 Unauthorized.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>Spring Security authentication enforcement.</li>
     *     <li>Protected endpoint access restrictions.</li>
     *     <li>Unauthorized response handling.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    void logMovement_withoutAuthentication_shouldReturnUnauthorised() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setPassportNumber("A1234567");
        request.setType(MovementType.ENTRY);
        request.setDate(LocalDateTime.of(2026, 5, 29,00,00));

        mockMvc.perform(post("/api/movements")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}