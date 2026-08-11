package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.LoginRequestDTO;
import com.cognizant.logitrack.dto.LoginResponseDTO;
import com.cognizant.logitrack.dto.RegisterRequestDTO;
import com.cognizant.logitrack.dto.UserDTO;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.enums.UserStatus;
import com.cognizant.logitrack.repository.UserRepository;
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
 * Authentication is a single stateless JWT access token. There is no refresh
 * token and no server-side session: the token is self-contained, signed, and
 * valid until it expires, at which point the client simply signs in again.
 *
 * Trade-off (accepted by design): because a signed JWT cannot be revoked,
 * deactivating a user blocks the NEXT login immediately but any token already
 * issued stays valid until it expires. Keep the access-token lifetime sensible
 * (see jwt.expiration) to bound that window.
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

    @Value("${jwt.expiration:86400000}")
    private long accessExpirationMs;

    public AuthController(UserService userService, UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                          AuditLogService auditLogService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogService = auditLogService;
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

        LoginResponseDTO response = issueSession(user);

        log.info("User logged in: {}", user.getEmail());
        audit(user.getUserId(), "LOGIN");

        return ResponseEntity.ok(response);
    }

    private LoginResponseDTO issueSession(User user) {
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());

        return LoginResponseDTO.builder()
                .token(accessToken)
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
