package visa_holder_tracker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class LoginSecurityConfig {
    /// Provider: How to authenticate
    /// Manager: Handles authentication process

    private final CustomUserDetailsService userDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http // Spring Security configuration object
                .csrf(csrf -> csrf // Disable CSRF protection (but allow H2 console)
                        .ignoringRequestMatchers("/h2-console/**")
                        .disable()
                )
                .httpBasic(AbstractHttpConfigurer::disable) // Disable http basic
                .formLogin(AbstractHttpConfigurer::disable) // Disable browser form login
                .headers(headers -> headers // Required for H2 console to work (frames)
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth // Manages endpoint authorization
                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                        .requestMatchers("/api/auth/login", "/api/auth/test").permitAll() // Permit any request to the mentioned endpoints
                        .anyRequest().authenticated() // Any other request fall under the default authentication
                );

        return http.build(); // Applies all the new rules
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }


    // Can be archived, this should now use the custom user details service
    // Also .setUserDetailsService is depreciated
    //  - Spring Security 6.5 prefers for UserDetailsService to be passed directly into the constructor
//    @Bean
//    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService){
//        // Use database backed authentication through UserDetailsService
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//
//        // Used to tell spring how to verify passwords using BCrypt
//        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
//
//        // Set where to load users from
//        provider.setUserDetailsService(userDetailsService);
//        return provider;
//    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        // get spring's built-in manager (Automatically uses our authenticationProvider)
        return config.getAuthenticationManager();
    }

    // Temporal for testing should be removed once we have our database grabbing the details
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User
                .withUsername("user")
                .password(new BCryptPasswordEncoder(12).encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User
                .withUsername("admin")
                .password(new BCryptPasswordEncoder(12).encode("password43"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }


}