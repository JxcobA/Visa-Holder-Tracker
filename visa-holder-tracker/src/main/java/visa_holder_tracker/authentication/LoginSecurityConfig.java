package visa_holder_tracker.authentication;

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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class LoginSecurityConfig {
    /// Provider: How to authenticate
    /// Manager: Handles authentication process

    private final JwtFilter jwtFilter;

    public LoginSecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http // Spring Security configuration object
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection
                .httpBasic(AbstractHttpConfigurer::disable) // Disable http basic
                .formLogin(AbstractHttpConfigurer::disable) // Disable browser form login
                .authorizeHttpRequests(auth -> auth // Manages endpoint authorization
                        .requestMatchers("/api/auth/login").permitAll() // Permit any request to the mentioned endpoints
                        .anyRequest().authenticated() // Any other request fall under the default authentication.
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Applies all the new rules.
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService){
        // Use database backed authentication through UserDetailsService
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // Used to tell spring how to verify passwords using BCrypt
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));

        // Set where to load users from
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        // get spring's built-in manager (Automatically uses our authenticationProvider)
        return config.getAuthenticationManager();
    }

    // Temporal for testing should be removed once we have our database getting the details
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