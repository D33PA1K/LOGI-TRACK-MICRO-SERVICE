package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.LoginRequestDTO;
import com.cognizant.logitrack.dto.LoginResponseDTO;
import com.cognizant.logitrack.dto.RegisterRequestDTO;
import com.cognizant.logitrack.dto.UserDTO;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.repository.UserRepository;
import com.cognizant.logitrack.service.UserService;
import com.cognizant.logitrack.service.AuditLogService;
import com.cognizant.logitrack.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    public AuthController(UserService userService, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuditLogService auditLogService) {
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
        User user = userOpt.get();
        if (user.getStatus() != com.cognizant.logitrack.enums.UserStatus.ACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "User account is inactive. Please contact your administrator."));
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());
        LoginResponseDTO response = LoginResponseDTO.builder().token(token).role(user.getRole().name()).userId(user.getUserId()).name(user.getName()).build();
        log.info("User logged in: {}", user.getEmail());
        try {
            auditLogService.logAction(user.getUserId(), "LOGIN", "User");
        } catch (Exception e) {
            log.warn("Failed to record LOGIN audit log: {}", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}



