package com.hermes.finance.domain.auth;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

@Component
public class RefreshTokenRowMapper implements RowMapper<RefreshToken> {

    @Override
    public RefreshToken mapRow(ResultSet rs, int rowNum) throws SQLException {
        RefreshToken token = new RefreshToken();
        token.setId(rs.getObject("id", java.util.UUID.class));
        token.setUserId(rs.getObject("user_id", java.util.UUID.class));
        token.setTokenHash(rs.getString("token_hash"));
        token.setExpiresAt(rs.getObject("expires_at", OffsetDateTime.class));
        token.setRevokedAt(rs.getObject("revoked_at", OffsetDateTime.class));
        token.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return token;
    }
}
