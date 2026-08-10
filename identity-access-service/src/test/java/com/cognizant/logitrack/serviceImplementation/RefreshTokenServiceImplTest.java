package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.entity.RefreshToken;
import com.cognizant.logitrack.entity.User;
import com.cognizant.logitrack.enums.Role;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    private static final long SEVEN_DAYS_MS = 604_800_000L;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private final User user = User.builder()
            .userId(1)
            .name("Admin")
            .email("admin@logitrack.com")
            .role(Role.ADMIN)
            .build();

    @BeforeEach
    void setExpiryProperty() {
        // @Value is not applied by Mockito, so set it the way Spring would.
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", SEVEN_DAYS_MS);
    }

    @Test
    @DisplayName("the raw token is returned to the caller but only its hash is persisted")
    void rawTokenIsNeverStored() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String rawToken = refreshTokenService.issue(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();

        assertNotNull(rawToken);
        assertNotEquals(rawToken, saved.getTokenHash(),
                "the raw token must not be written to the database");
        assertEquals(64, saved.getTokenHash().length(), "SHA-256 hex is 64 characters");
        assertFalse(saved.isRevoked());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("two issued tokens are different (the value is random, not derived)")
    void issuedTokensAreUnique() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertNotEquals(refreshTokenService.issue(user), refreshTokenService.issue(user));
    }

    @Test
    @DisplayName("a valid token resolves to its row")
    void validTokenIsAccepted() {
        RefreshToken stored = RefreshToken.builder()
                .tokenId(1)
                .user(user)
                .tokenHash("ignored-because-we-stub-the-lookup")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        assertSame(stored, refreshTokenService.validate("some-raw-token"));
    }

    @Test
    @DisplayName("a revoked token is rejected — this is what ends a deactivated user's session")
    void revokedTokenIsRejected() {
        RefreshToken revoked = RefreshToken.builder()
                .tokenId(1)
                .user(user)
                .tokenHash("hash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revoked));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> refreshTokenService.validate("some-raw-token"));

        assertTrue(error.getMessage().contains("revoked"), error.getMessage());
    }

    @Test
    @DisplayName("an expired token is rejected")
    void expiredTokenIsRejected() {
        RefreshToken expired = RefreshToken.builder()
                .tokenId(1)
                .user(user)
                .tokenHash("hash")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> refreshTokenService.validate("some-raw-token"));

        assertTrue(error.getMessage().contains("expired"), error.getMessage());
    }

    @Test
    @DisplayName("an unrecognised token is rejected")
    void unknownTokenIsRejected() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> refreshTokenService.validate("nope"));
    }

    @Test
    @DisplayName("a blank token is rejected without hitting the database")
    void blankTokenIsRejected() {
        assertThrows(BadRequestException.class, () -> refreshTokenService.validate("  "));
        assertThrows(BadRequestException.class, () -> refreshTokenService.validate(null));

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("revoking marks the row revoked and stamps when")
    void revokeMarksTheRow() {
        RefreshToken stored = RefreshToken.builder()
                .tokenId(1)
                .user(user)
                .tokenHash("hash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        refreshTokenService.revoke("some-raw-token");

        assertTrue(stored.isRevoked());
        assertNotNull(stored.getRevokedAt());
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    @DisplayName("revoking an already-revoked token is a silent no-op")
    void revokingTwiceIsIdempotent() {
        RefreshToken alreadyRevoked = RefreshToken.builder()
                .tokenId(1)
                .user(user)
                .tokenHash("hash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(true)
                .revokedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(alreadyRevoked));

        assertDoesNotThrow(() -> refreshTokenService.revoke("some-raw-token"));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("revoking an unknown token does not throw — there is nothing left to end")
    void revokingUnknownTokenIsSilent() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> refreshTokenService.revoke("nope"));
    }

    @Test
    @DisplayName("revokeAllForUser tolerates a null id")
    void revokeAllHandlesNullUserId() {
        assertEquals(0, refreshTokenService.revokeAllForUser(null));
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("the same raw token always hashes to the same value, so lookup works")
    void hashingIsDeterministic() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> refreshTokenService.validate("token-abc"));
        assertThrows(BadRequestException.class, () -> refreshTokenService.validate("token-abc"));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenRepository, times(2)).findByTokenHash(captor.capture());

        assertEquals(captor.getAllValues().get(0), captor.getAllValues().get(1));
    }
}
