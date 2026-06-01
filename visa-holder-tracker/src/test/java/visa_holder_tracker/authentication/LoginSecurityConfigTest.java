package visa_holder_tracker.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.test.web.servlet.MockMvc;
import visa_holder_tracker.config.CustomUserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Integration and unit tests for {@link LoginSecurityConfig}.
 *
 * <p>
 * These tests validate:
 * <ul>
 *     <li>Spring Security endpoint access rules.</li>
 *     <li>Authentication manager bean creation.</li>
 *     <li>Authentication provider configuration.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Uses:
 * <ul>
 *     <li>{@link SpringBootTest} to load the full Spring context.</li>
 *     <li>{@link AutoConfigureMockMvc} for HTTP endpoint testing.</li>
 *     <li>{@link MockMvc} to simulate API requests.</li>
 * </ul>
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginSecurityConfigTest {


    /**
     * Mock MVC client used to simulate HTTP requests
     * against secured endpoints.
     */
    @Autowired
    MockMvc mockMvc;


    /**
     * Spring Security authentication manager bean
     * loaded from the application context.
     */
    @Autowired
    AuthenticationManager authenticationManager;

    // --- securityFilterChain: assert the RULES via real HTTP behaviour ---


    /**
     * Verifies that public endpoints configured with
     * {@code permitAll()} are accessible without authentication.
     *
     * <p>
     * This test checks that:
     * <ul>
     *     <li>The endpoint is excluded from authentication requirements.</li>
     *     <li>The security filter chain allows anonymous access.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    void permitAllEndpoint_isReachableWithoutToken() throws Exception {
        // /actuator/health is in the permitAll list
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that protected endpoints return HTTP 401
     * when accessed without a JWT token.
     *
     * <p>
     * This test validates:
     * <ul>
     *     <li>{@code anyRequest().authenticated()} security enforcement.</li>
     *     <li>The custom authentication entry point behavior.</li>
     *     <li>Unauthorized requests return 401 instead of 403.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        // anyRequest().authenticated() + the custom entry point => 401, not 403
        mockMvc.perform(get("/api/visa-holders"))
                .andExpect(status().isUnauthorized());
    }

    // --- authenticationManager: the context wired a real manager ---

    /**
     * Verifies that the Spring application context
     * successfully creates an {@link AuthenticationManager} bean.
     *
     * <p>
     * Confirms that Spring Security authentication
     * infrastructure is correctly configured.
     * </p>
     */
    @Test
    void authenticationManager_beanIsAvailable() {
        assertThat(authenticationManager).isNotNull();
    }

    // --- authenticationProvider: plain unit test, no context, no Mockito ---

    /**
     * Verifies that the configured authentication provider
     * is a {@link DaoAuthenticationProvider}.
     *
     * <p>
     * This test ensures:
     * <ul>
     *     <li>Database-backed authentication is being used.</li>
     *     <li>The security configuration returns the expected provider type.</li>
     * </ul>
     * </p>
     *
     * <p>
     * This is a lightweight unit test that does not
     * require the Spring application context.
     * </p>
     */
    @Test
    void authenticationProvider_isDaoProvider() {
        LoginSecurityConfig config = new LoginSecurityConfig(null);            // jwtFilter unused by this method
        CustomUserDetailsService uds = new CustomUserDetailsService(null, null); // repos never dereferenced here
        AuthenticationProvider provider = config.authenticationProvider(uds);
        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
    }
}