package com.hermes.finance.domain.auth;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByHash(String tokenHash);
    void revoke(String tokenHash);
    void revokeAllForUser(UUID userId);
}
