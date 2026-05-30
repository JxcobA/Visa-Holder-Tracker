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

@Configuration // Becomes the configuration bean class
@EnableWebSecurity // Enables endpoint security for request access
@EnableMethodSecurity // Enables method security for role access
public class LoginSecurityConfig {
    /// Provider: How to authenticate
    /// Manager: Handles authentication process

    // Injects our custom JWT filter
    private final JwtFilter jwtFilter;

    // Constructor to inject JWT Filter (Maybe I should do autowired instead)
    public LoginSecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


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
                        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll() // Permit any request to the mentioned endpoints
                        .anyRequest().authenticated() // Any other request fall under the default authentication.
                )
                .exceptionHandling(ex -> ex // Registeres an entry point so 401 doesn't fall back to 403
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                        )
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Applies all the new rules.
    }

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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        // get spring's built-in manager (Automatically uses our authenticationProvider)
        return config.getAuthenticationManager();
    }

    // Temporal for testing should be removed once we have our database grabbing the details
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = User
//                .withUsername("user")
//                .password(new BCryptPasswordEncoder(12).encode("password"))
//                .roles("USER")
//                .build();
//
//        UserDetails admin = User
//                .withUsername("admin")
//                .password(new BCryptPasswordEncoder(12).encode("password43"))
//                .roles("ADMIN")
//                .build();
//
//        return new InMemoryUserDetailsManager(user, admin);
//    }


}