package com.cognizant.logitrack.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Reads the authenticated caller's identity for the current request:
 *  - the role comes from the Spring SecurityContext (set by JwtFilter),
 *  - the numeric userId comes from the "userId" claim inside the JWT.
 * Used to enforce "a shipper can only act as themselves" on the server side,
 * independent of whatever the request body claims.
 */
@Component
public class CurrentUserProvider {

    private final JwtUtil jwtUtil;

    public CurrentUserProvider(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /** True if the current caller has exactly the given role (e.g. "SHIPPER"). */
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_" + role));
    }

    /** The current caller's userId from the JWT, or null if it can't be read. */
    public Integer getCurrentUserId() {
        String token = extractToken();
        if (token == null) {
            return null;
        }
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        String header = attributes.getRequest().getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
