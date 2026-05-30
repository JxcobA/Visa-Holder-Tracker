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


// Such that it becomes a bean
@Component
public class JwtFilter extends OncePerRequestFilter {
    // Extends from OncePerRequestFilter: to check JWT after every client request.

    // Auto-injects the GenerateJWTToken bean in the JWT filter class
    @Autowired
    GenerateJWTToken jwtService; // Helps read and parse tokens

    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Gets the JWT header
        String header = request.getHeader("Authorization");

        String path = request.getServletPath();


        if (path.equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }


        // Checks for the valid JWT format
        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);

                System.out.println("JWT username: " + username);
                System.out.println("JWT role: " + role);

                UsernamePasswordAuthenticationToken user =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );

                SecurityContextHolder.getContext().setAuthentication(user);
            }
        } catch (Exception e) {
            System.out.println("JWT ERROR: " + e.getMessage());
        }

        // Continues to the request
        filterChain.doFilter(request, response);
    }
}