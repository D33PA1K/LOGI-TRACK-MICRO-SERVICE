package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revokes every live token for a user in one statement — used when an admin
     * deactivates or deletes the account, so the session ends immediately rather
     * than at the next access-token expiry.
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now "
            + "WHERE r.user.userId = :userId AND r.revoked = false")
    int revokeAllForUser(@Param("userId") Integer userId, @Param("now") LocalDateTime now);

    long countByUser_UserIdAndRevokedFalse(Integer userId);
}
