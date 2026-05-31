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

@SpringBootTest
@AutoConfigureMockMvc
class LoginSecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AuthenticationManager authenticationManager;

    // --- securityFilterChain: assert the RULES via real HTTP behaviour ---

    @Test
    void permitAllEndpoint_isReachableWithoutToken() throws Exception {
        // /actuator/health is in the permitAll list
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        // anyRequest().authenticated() + the custom entry point => 401, not 403
        mockMvc.perform(get("/api/visa-holders"))
                .andExpect(status().isUnauthorized());
    }

    // --- authenticationManager: the context wired a real manager ---

    @Test
    void authenticationManager_beanIsAvailable() {
        assertThat(authenticationManager).isNotNull();
    }

    // --- authenticationProvider: plain unit test, no context, no Mockito ---

    @Test
    void authenticationProvider_isDaoProvider() {
        LoginSecurityConfig config = new LoginSecurityConfig(null);            // jwtFilter unused by this method
        CustomUserDetailsService uds = new CustomUserDetailsService(null, null); // repos never dereferenced here
        AuthenticationProvider provider = config.authenticationProvider(uds);
        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
    }
}