package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.entity.RefreshToken;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.repository.RefreshTokenRepository;
import com.cognizant.logitrack.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int TOKEN_BYTES = 32; // 256 bits of entropy

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public String issue(User user) {
        byte[] raw = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(raw);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        RefreshToken token = RefreshToken.builder()
                .tokenHash(hash(rawToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .revoked(false)
                .build();

        refreshTokenRepository.save(token);
        log.debug("Refresh token issued for userId={}", user.getUserId());

        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("A refresh token is required.");
        }

        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BadRequestException(
                        "Refresh token is not recognised. Please sign in again."));

        if (token.isRevoked()) {
            throw new BadRequestException("This session has been revoked. Please sign in again.");
        }

        if (token.getExpiresAt() == null || !token.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("This session has expired. Please sign in again.");
        }

        return token;
    }

    @Override
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        Optional<RefreshToken> existing = refreshTokenRepository.findByTokenHash(hash(rawToken));

        existing.ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevoked(true);
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    @Override
    public int revokeAllForUser(Integer userId) {
        if (userId == null) {
            return 0;
        }

        int revoked = refreshTokenRepository.revokeAllForUser(userId, LocalDateTime.now());

        if (revoked > 0) {
            log.info("Revoked {} refresh token(s) for userId={}", revoked, userId);
        }

        return revoked;
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hashed.length * 2);

            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JDK; unreachable in practice.
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
