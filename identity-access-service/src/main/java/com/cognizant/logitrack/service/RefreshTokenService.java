package com.cognizant.logitrack.service;

import com.cognizant.logitrack.entity.RefreshToken;
import com.cognizant.logitrack.entity.User;

public interface RefreshTokenService {

    /** Issues a new refresh token and returns the RAW value (only ever seen here and by the client). */
    String issue(User user);

    /** Resolves a raw token to its live row, or throws if unknown/expired/revoked. */
    RefreshToken validate(String rawToken);

    /** Revokes the supplied token. Silent when the token is already unknown or revoked. */
    void revoke(String rawToken);

    /** Revokes every live token for a user. Returns how many were revoked. */
    int revokeAllForUser(Integer userId);
}
