package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.LoginRequestDTO;
import com.cognizant.logitrack.dto.LoginResponseDTO;
import com.cognizant.logitrack.dto.RefreshRequestDTO;
import com.cognizant.logitrack.dto.RegisterRequestDTO;
import com.cognizant.logitrack.dto.UserDTO;
import com.cognizant.logitrack.entity.RefreshToken;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.enums.UserStatus;
import com.cognizant.logitrack.repository.UserRepository;
import com.cognizant.logitrack.service.RefreshTokenService;
import com.cognizant.logitrack.service.UserService;
import com.cognizant.logitrack.service.AuditLogService;
import com.cognizant.logitrack.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

/**
 * Session lifecycle.
 *
 * The access token is a short-lived JWT (30 minutes by default) and the durable
 * half of the session is a revocable refresh token. That split is what makes
 * "deactivate this user" take effect promptly: a signed JWT cannot be recalled,
 * but the refresh token can be, and the short access lifetime bounds the window.
 *
 * Refresh tokens are ROTATED on every use — the presented token is revoked and a
 * new one issued — so a stolen token is usable at most once before the legitimate
 * client's next refresh invalidates it.
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration:1800000}")
    private long accessExpirationMs;

    public AuthController(UserService userService, UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                          AuditLogService auditLogService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogService = auditLogService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        UserDTO created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(dto.getPassword(), userOpt.get().getPasswordHash())) {
            // A wrong password on a known account is a security-relevant event, so
            // it is audited. An unknown email has no user row to attribute it to.
            userOpt.ifPresent(user -> audit(user.getUserId(), "LOGIN_FAILED"));

            // Deliberately the same message for "no such user" and "wrong password"
            // so the response cannot be used to enumerate valid accounts.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();

        if (user.getStatus() != UserStatus.ACTIVE) {
            audit(user.getUserId(), "LOGIN_BLOCKED_INACTIVE");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "User account is inactive. Please contact your administrator."));
        }

        LoginResponseDTO response = issueSession(user, refreshTokenService.issue(user));

        log.info("User logged in: {}", user.getEmail());
        audit(user.getUserId(), "LOGIN");

        return ResponseEntity.ok(response);
    }

    /**
     * Exchanges a refresh token for a fresh access token, re-checking that the
     * account is still ACTIVE — this is the point at which an admin's deactivation
     * takes effect on an already-signed-in user.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequestDTO dto) {
        RefreshToken existing = refreshTokenService.validate(dto.getRefreshToken());
        User user = existing.getUser();

        if (user.getStatus() != UserStatus.ACTIVE) {
            // Do not leave a usable session behind for a disabled account.
            refreshTokenService.revokeAllForUser(user.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "User account is inactive. Please contact your administrator."));
        }

        // Rotate: the presented token is spent, a new one takes its place.
        refreshTokenService.revoke(dto.getRefreshToken());
        String rotated = refreshTokenService.issue(user);

        log.debug("Access token refreshed for {}", user.getEmail());

        return ResponseEntity.ok(issueSession(user, rotated));
    }

    /**
     * Ends the session by revoking the refresh token. Always reports success:
     * possessing the token is the only authorization required, and an unknown
     * token means there is nothing left to end.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequestDTO dto) {
        if (dto != null) {
            refreshTokenService.revoke(dto.getRefreshToken());
        }

        return ResponseEntity.noContent().build();
    }

    private LoginResponseDTO issueSession(User user, String refreshToken) {
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());

        return LoginResponseDTO.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresInSeconds(accessExpirationMs / 1000)
                .role(user.getRole().name())
                .userId(user.getUserId())
                .name(user.getName())
                .build();
    }

    // Auditing must never break authentication.
    private void audit(Integer userId, String action) {
        try {
            auditLogService.logAction(userId, action, "User");
        } catch (Exception e) {
            log.warn("Failed to record {} audit log: {}", action, e.getMessage());
        }
    }
}
