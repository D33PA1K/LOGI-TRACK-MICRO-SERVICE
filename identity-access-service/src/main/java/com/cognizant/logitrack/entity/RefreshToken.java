package com.cognizant.logitrack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A revocable, server-side refresh token.
 *
 * This is the piece that makes deactivating a user actually end their session.
 * A bare JWT cannot be revoked — once signed it is valid until it expires — so
 * the access token is kept short-lived and the long-lived half of the session
 * lives here, in a row we can revoke at will.
 *
 * Only the SHA-256 hash of the token is stored: a database dump therefore does
 * not hand out usable sessions. The hash is unsalted on purpose — the token is
 * 256 bits of secure randomness, so there is no dictionary to attack, and an
 * unsalted digest is what lets us look a token up by value.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "tokenHash")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tokenId;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean revoked = false;

    private LocalDateTime revokedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public boolean isUsable() {
        return !revoked && expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }
}
