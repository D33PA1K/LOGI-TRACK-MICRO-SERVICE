package com.cognizant.logitrack.config;
 
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
                                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/freight-orders").hasAnyRole("SHIPPER", "COORDINATOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/freight-orders/*/cancel").hasAnyRole("COORDINATOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/freight-orders/**").hasAnyRole("SHIPPER", "COORDINATOR", "ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shipments").hasAnyRole("COORDINATOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shipments/*/dispatch").hasAnyRole("COORDINATOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/shipments/*/status").hasAnyRole("COORDINATOR", "DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shipments/*/events").hasAnyRole("DRIVER", "COORDINATOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/shipments/**").hasAnyRole("SHIPPER", "COORDINATOR", "DRIVER", "ANALYST", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
 
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
