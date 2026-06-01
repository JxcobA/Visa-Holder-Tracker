package visa_holder_tracker.jwt_utils;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;



/**
 * JWT authentication filter that intercepts every incoming HTTP request
 * and validates the JWT token provided in the Authorization header.
 *
 * <p>
 * This filter:
 * <ul>
 *     <li>Skips authentication for the login endpoint.</li>
 *     <li>Extracts the JWT token from the Authorization header.</li>
 *     <li>Validates and parses the token.</li>
 *     <li>Extracts the username and role from the token.</li>
 *     <li>Sets the authenticated user into the Spring Security context.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Registered as a Spring Bean using the {@code @Component} annotation.
 * </p>
 */
@Component // Let spring boot manage it.
public class JwtFilter extends OncePerRequestFilter {
    // Extends from OncePerRequestFilter: to check JWT after every client request.

    // Auto-injects the GenerateJWTToken bean in the JWT filter class
    @Autowired
    GenerateJwtToken jwtService; // Helps read and parse tokens


    /**
     * Filters every incoming request and performs JWT authentication.
     *
     * <p>
     * The method:
     * <ul>
     *     <li>Reads the Authorization header.</li>
     *     <li>Skips authentication for the login endpoint.</li>
     *     <li>Extracts and validates the JWT token.</li>
     *     <li>Creates an authenticated Spring Security token.</li>
     *     <li>Stores authentication details in the SecurityContext.</li>
     * </ul>
     * </p>
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param filterChain the filter chain used to continue request processing
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an input or output error occurs
     */
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Reads the JWT authorization header
        String header = request.getHeader("Authorization");

        // Reads the request path e.g. /api/auth/login
        String path = request.getServletPath();

        // We explicitly stop the login endpoint from having to
        if (path.equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (header != null && header.startsWith("Bearer ")) {
            // Removes the bearer prefix to only keep the token
            String token = header.substring(7);
            try {
                // Extracts the username from the token
                String username = jwtService.extractUsername(token);
                // Extracts the role from the token
                String role = jwtService.extractRole(token);

                var authToken = new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        // Continues to the request
        filterChain.doFilter(request, response);
    }
}