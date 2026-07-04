package com.hermes.finance.domain.auth;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepository implements RefreshTokenRepositoryPort {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NativeQueryCatalog nativeQueryCatalog;
    private final RefreshTokenRowMapper rowMapper;

    public RefreshTokenRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                  NativeQueryCatalog nativeQueryCatalog,
                                  RefreshTokenRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.nativeQueryCatalog = nativeQueryCatalog;
        this.rowMapper = rowMapper;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        UUID id = token.getId() != null ? token.getId() : UUID.randomUUID();
        OffsetDateTime createdAt = token.getCreatedAt() != null ? token.getCreatedAt() : OffsetDateTime.now();
        String sql = nativeQueryCatalog.get("refreshToken.insert");
        jdbcTemplate.update(sql, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", token.getUserId())
            .addValue("tokenHash", token.getTokenHash())
            .addValue("expiresAt", token.getExpiresAt())
            .addValue("revokedAt", token.getRevokedAt())
            .addValue("createdAt", createdAt));
        token.setId(id);
        token.setCreatedAt(createdAt);
        return token;
    }

    @Override
    public Optional<RefreshToken> findByHash(String tokenHash) {
        String sql = nativeQueryCatalog.get("refreshToken.findByHash");
        return jdbcTemplate.query(sql, new MapSqlParameterSource()
            .addValue("tokenHash", tokenHash), rowMapper).stream().findFirst();
    }

    @Override
    public void revoke(String tokenHash) {
        String sql = nativeQueryCatalog.get("refreshToken.revoke");
        jdbcTemplate.update(sql, new MapSqlParameterSource()
            .addValue("tokenHash", tokenHash)
            .addValue("revokedAt", OffsetDateTime.now()));
    }

    @Override
    public void revokeAllForUser(UUID userId) {
        String sql = nativeQueryCatalog.get("refreshToken.revokeAllForUser");
        jdbcTemplate.update(sql, new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("revokedAt", OffsetDateTime.now()));
    }
}
