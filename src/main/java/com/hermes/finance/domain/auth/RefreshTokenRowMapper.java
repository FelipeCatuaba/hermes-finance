package com.hermes.finance.domain.auth;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class RefreshTokenRowMapper implements RowMapper<RefreshToken> {
    @Override
    public RefreshToken mapRow(ResultSet rs, int rowNum) throws SQLException {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.fromString(rs.getString("id")));
        token.setUserId(UUID.fromString(rs.getString("user_id")));
        token.setToken(rs.getString("token"));
        token.setExpiresAt(rs.getObject("expires_at", OffsetDateTime.class));
        token.setRevoked(rs.getBoolean("revoked"));
        token.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return token;
    }
}
