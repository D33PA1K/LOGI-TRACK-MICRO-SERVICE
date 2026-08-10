package com.cognizant.logitrack.security;

import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves the authenticated caller for audit purposes.
 *
 * The principal set by {@link JwtFilter} is the email from the token's subject
 * claim, so the actor is resolved against the users table this service already
 * owns. Deliberately NOT read from the X-User-Id header: that header is injected
 * by the gateway and is trustworthy there, but resolving from our own verified
 * SecurityContext keeps this service independent of the gateway.
 */
@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = authentication.getName();

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return userRepository.findByEmail(email);
    }

    public Integer getCurrentUserId() {
        return getCurrentUser().map(User::getUserId).orElse(null);
    }
}
