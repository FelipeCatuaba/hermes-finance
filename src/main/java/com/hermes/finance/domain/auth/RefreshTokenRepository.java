package com.hermes.finance.domain.auth;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepository {

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

    public RefreshToken save(RefreshToken refreshToken) {
        UUID id = refreshToken.getId() != null ? refreshToken.getId() : UUID.randomUUID();
        OffsetDateTime createdAt = refreshToken.getCreatedAt() != null ? refreshToken.getCreatedAt() : OffsetDateTime.now();

        String sql = nativeQueryCatalog.get("refreshToken.insert");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", refreshToken.getUserId())
            .addValue("token", refreshToken.getToken())
            .addValue("expiresAt", refreshToken.getExpiresAt())
            .addValue("revoked", refreshToken.isRevoked())
            .addValue("createdAt", createdAt);

        jdbcTemplate.update(sql, params);
        refreshToken.setId(id);
        refreshToken.setCreatedAt(createdAt);
        return refreshToken;
    }

    public Optional<RefreshToken> findActiveByToken(String hashedToken) {
        String sql = nativeQueryCatalog.get("refreshToken.findActiveByToken");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("token", hashedToken);

        return jdbcTemplate.query(sql, params, rowMapper).stream().findFirst();
    }

    public void revokeById(UUID id) {
        String sql = nativeQueryCatalog.get("refreshToken.revokeById");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id);
        jdbcTemplate.update(sql, params);
    }

    public void revokeAllByUserId(UUID userId) {
        String sql = nativeQueryCatalog.get("refreshToken.revokeAllByUserId");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }
}
