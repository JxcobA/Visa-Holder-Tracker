package visa_holder_tracker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import visa_holder_tracker.entity.*;
import visa_holder_tracker.repository.AdminRepository;
import visa_holder_tracker.repository.MovementRepository;
import visa_holder_tracker.repository.UserRepository;
import visa_holder_tracker.repository.VisaHolderRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Full integration tests for role-based access control.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>JWT authentication behavior.</li>
 *     <li>Role-based authorization rules.</li>
 *     <li>ADMIN-only endpoint restrictions.</li>
 *     <li>Spring Security access enforcement.</li>
 * </ul>
 * </p>
 *
 * <p>
 * These tests run against the real Spring Boot
 * application context, security configuration,
 * JWT authentication flow, and database layer.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RoleRestrictionTest {

    /**
     * Mock MVC client used to simulate
     * authenticated HTTP requests.
     */
    @Autowired
    MockMvc mockMvc;

    /**
     * Repository used for USER account setup.
     */
    @Autowired
    UserRepository userRepository;

    /**
     * Repository used for ADMIN account setup.
     */
    @Autowired
    AdminRepository adminRepository;

    /**
     * Repository used for visa holder
     * persistence and deletion tests.
     */
    @Autowired
    VisaHolderRepository visaHolderRepository;

    /**
     * Repository used for movement cleanup
     * due to foreign key relationships.
     */
    @Autowired
    MovementRepository movementRepository;


    /**
     * Verifies that USER accounts
     * cannot access ADMIN-only delete endpoints.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>JWT authentication success.</li>
     *     <li>Authorization failure for USER roles.</li>
     *     <li>HTTP 403 Forbidden responses.</li>
     * </ul>
     * </p>
     */
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


    /**
     * Verifies that ADMIN accounts
     * can successfully delete visa holders.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>JWT authentication success.</li>
     *     <li>ADMIN role authorization.</li>
     *     <li>Visa holder deletion behavior.</li>
     *     <li>HTTP 204 No Content responses.</li>
     * </ul>
     * </p>
     */
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

    /**
     * Clears database state after each test.
     *
     * <p>
     * Ensures test isolation by removing:
     * <ul>
     *     <li>Movement records</li>
     *     <li>Visa holder records</li>
     *     <li>Administrator accounts</li>
     *     <li>User accounts</li>
     * </ul>
     * </p>
     */
    @AfterEach
    void cleanUp() {
        movementRepository.deleteAll();
        visaHolderRepository.deleteAll();
        adminRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Performs authentication and retrieves
     * a JWT token for secured endpoint testing.
     *
     * <p>
     * This helper method:
     * <ul>
     *     <li>Sends login credentials to the authentication endpoint.</li>
     *     <li>Extracts the JWT token from the response body.</li>
     *     <li>Returns the token for authenticated requests.</li>
     * </ul>
     * </p>
     *
     * @param username login username
     * @param password login password
     * @return generated JWT token
     * @throws Exception if authentication fails
     */
    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(response, "$.token");
    }

}
