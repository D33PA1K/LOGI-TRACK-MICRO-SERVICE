package com.cognizant.logitrack.config;
 
import jakarta.servlet.http.HttpServletResponse;
import com.cognizant.logitrack.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
 
    private final JwtFilter jwtFilter;
    private final UrlBasedCorsConfigurationSource corsConfigurationSource;
 
    public SecurityConfig(JwtFilter jwtFilter, UrlBasedCorsConfigurationSource corsConfigurationSource) {
        this.jwtFilter = jwtFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Correct HTTP semantics: 401 means "not authenticated", 403 means
                // "authenticated but not allowed". Without an explicit entry point
                // Spring Security answers 403 to an anonymous request, which hides an
                // expired access token from the client and prevents it from
                // triggering a token refresh.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentication required"))
                        .accessDeniedHandler((request, response, ex) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "You do not have permission to perform this action")))
                                .authorizeHttpRequests(auth -> auth
                        // Shippers need to look up an active route when booking a freight order,
                        // so GET on routes is open to SHIPPER as well (carriers/rate-cards stay COORDINATOR/ADMIN).
                        .requestMatchers(HttpMethod.GET, "/api/routes/**").hasAnyRole("SHIPPER", "COORDINATOR", "ANALYST", "ADMIN")
                        // ANALYST reads carriers so logistics-analytics-service can label
                        // carrier scorecards with a real name instead of a bare id. Rate
                        // cards stay closed to analysts — pricing is not needed for KPIs.
                        .requestMatchers(HttpMethod.GET, "/api/carriers/**").hasAnyRole("COORDINATOR", "ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/rate-cards/**").hasAnyRole("COORDINATOR", "ADMIN")
                        .requestMatchers("/api/routes/**", "/api/carriers/**", "/api/rate-cards/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
 
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void writeError(HttpServletResponse response, int status, String message)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + message + "\"}");
    }
}
