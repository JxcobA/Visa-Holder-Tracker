package visa_holder_tracker.authentication;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import visa_holder_tracker.config.CustomUserDetailsService;
import visa_holder_tracker.jwt_utils.JwtFilter;



/**
 * Spring Security configuration class responsible for configuring
 * authentication, authorization, JWT filtering, and security behavior
 * across the application.
 *
 * <p>
 * This configuration:
 * <ul>
 *     <li>Disables default form login and HTTP basic authentication.</li>
 *     <li>Registers JWT authentication filtering.</li>
 *     <li>Defines public and protected endpoints.</li>
 *     <li>Configures password encoding using BCrypt.</li>
 *     <li>Provides authentication manager and provider beans.</li>
 * </ul>
 * </p>
 */
@Configuration // Becomes the configuration bean class
@EnableWebSecurity // Enables endpoint security for request access
@EnableMethodSecurity // Enables method security for role access
public class LoginSecurityConfig {
    /// Provider: How to authenticate
    /// Manager: Handles authentication process


    /**
     * Custom JWT filter used to validate JWT tokens
     * before request authentication is processed.
     */
    private final JwtFilter jwtFilter;


    /**
     * Constructor used for dependency injection of the JWT filter.
     *
     * @param jwtFilter custom JWT authentication filter
     */
    public LoginSecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


    /**
     * Configures the application's Spring Security filter chain.
     *
     * <p>
     * This configuration:
     * <ul>
     *     <li>Disables CSRF protection.</li>
     *     <li>Disables HTTP basic authentication.</li>
     *     <li>Disables form-based login.</li>
     *     <li>Allows access to public endpoints.</li>
     *     <li>Requires authentication for all other endpoints.</li>
     *     <li>Registers the JWT filter before Spring authentication processing.</li>
     * </ul>
     * </p>
     *
     * @param http Spring Security HTTP configuration object
     * @return configured {@link SecurityFilterChain}
     * @throws Exception if security configuration fails
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http // Spring Security configuration object
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection (but allow H2 console)
                .httpBasic(AbstractHttpConfigurer::disable) // Disable http basic
                .formLogin(AbstractHttpConfigurer::disable) // Disable browser form login
                .headers(headers -> headers // Required for H2 console to work (frames)
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth // Manages endpoint authorization
                        // Allow H2 console access.
                        .requestMatchers("/api/auth/login", "/h2-console/**", "/actuator/health",
                                "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Permit any request to the mentioned endpoints
                        .anyRequest().authenticated() // Any other request fall under the default authentication.
                )
                .exceptionHandling(ex -> ex // Registers an entry point so 401 doesn't fall back to 403
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                        )
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Apply all the new rules.
    }


    /**
     * Creates and configures the authentication provider used
     * for validating user credentials.
     *
     * <p>
     * Uses:
     * <ul>
     *     <li>{@link CustomUserDetailsService} for loading users.</li>
     *     <li>{@link BCryptPasswordEncoder} for password verification.</li>
     * </ul>
     * </p>
     *
     * @param userDetailsService custom service for loading user details
     * @return configured {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService){
        // Use database backed authentication through UserDetailsService
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // Used to tell spring how to verify passwords using BCrypt
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));

        // Set where to load users from (In memory, at least until I have a database to work with)
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }


    /**
     * Creates the application's authentication manager.
     *
     * <p>
     * The authentication manager delegates authentication
     * processing to the configured authentication provider.
     * </p>
     *
     * @param config Spring authentication configuration
     * @return configured {@link AuthenticationManager}
     * @throws Exception if the authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        // get spring's built-in manager (Automatically uses our authenticationProvider)
        return config.getAuthenticationManager();
    }

}