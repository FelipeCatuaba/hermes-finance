package com.hermes.finance.domain.share;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ShareTokenRepository implements ShareTokenRepositoryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NativeQueryCatalog nativeQueryCatalog;

    public ShareTokenRepository(NamedParameterJdbcTemplate jdbcTemplate, NativeQueryCatalog nativeQueryCatalog) {
        this.jdbcTemplate = jdbcTemplate;
        this.nativeQueryCatalog = nativeQueryCatalog;
    }

    @Override
    public void revokeActiveFor(UUID userId, UUID familyMemberId, int month, int year, OffsetDateTime revokedAt) {
        String sql = nativeQueryCatalog.get("shareToken.revokeActiveFor");
        jdbcTemplate.update(sql, new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("familyMemberId", familyMemberId)
            .addValue("month", month)
            .addValue("year", year)
            .addValue("revokedAt", revokedAt));
    }

    @Override
    public ShareToken save(ShareToken token) {
        String sql = nativeQueryCatalog.get("shareToken.insert");
        ShareToken saved = jdbcTemplate.queryForObject(sql, params(token), this::mapToken);
        if (saved == null) {
            throw new IllegalStateException("Share token insert did not return a row");
        }
        return saved;
    }

    @Override
    public List<ShareToken> findByUserId(UUID userId) {
        String sql = nativeQueryCatalog.get("shareToken.findByUserId");
        return jdbcTemplate.query(sql, new MapSqlParameterSource("userId", userId), this::mapToken);
    }

    @Override
    public Optional<ShareToken> findById(UUID id) {
        String sql = nativeQueryCatalog.get("shareToken.findById");
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), this::mapToken));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public void revokeById(UUID id, UUID userId, OffsetDateTime revokedAt) {
        String sql = nativeQueryCatalog.get("shareToken.revokeById");
        jdbcTemplate.update(sql, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", userId)
            .addValue("revokedAt", revokedAt));
    }

    @Override
    public Optional<ShareToken> findValidByHash(String tokenHash, OffsetDateTime now) {
        String sql = nativeQueryCatalog.get("shareToken.findValidByHash");
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new MapSqlParameterSource()
                .addValue("tokenHash", tokenHash)
                .addValue("now", now), this::mapToken));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<PublicShareExpenseItem> findPublicExpenses(UUID userId, UUID familyMemberId, LocalDate startDate, LocalDate endDate) {
        String sql = nativeQueryCatalog.get("shareToken.publicExpenses");
        return jdbcTemplate.query(sql, new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("familyMemberId", familyMemberId)
            .addValue("startDate", startDate)
            .addValue("endDate", endDate), this::mapPublicExpense);
    }

    @Override
    public boolean familyMemberBelongsToUser(UUID familyMemberId, UUID userId) {
        String sql = nativeQueryCatalog.get("shareToken.familyMemberBelongsToUser");
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource()
            .addValue("familyMemberId", familyMemberId)
            .addValue("userId", userId), Integer.class);
        return count != null && count > 0;
    }

    private MapSqlParameterSource params(ShareToken token) {
        return new MapSqlParameterSource()
            .addValue("id", token.getId())
            .addValue("userId", token.getUserId())
            .addValue("familyMemberId", token.getFamilyMemberId())
            .addValue("month", token.getMonth())
            .addValue("year", token.getYear())
            .addValue("tokenHash", token.getTokenHash())
            .addValue("expiresAt", token.getExpiresAt())
            .addValue("createdAt", token.getCreatedAt());
    }

    private ShareToken mapToken(ResultSet rs, int rowNum) throws SQLException {
        ShareToken token = new ShareToken();
        token.setId(rs.getObject("id", UUID.class));
        token.setUserId(rs.getObject("user_id", UUID.class));
        token.setFamilyMemberId(rs.getObject("family_member_id", UUID.class));
        token.setFamilyMemberName(rs.getString("family_member_name"));
        token.setFamilyMemberRelation(rs.getString("family_member_relation"));
        token.setMonth(rs.getInt("month"));
        token.setYear(rs.getInt("year"));
        token.setTokenHash(rs.getString("token_hash"));
        token.setExpiresAt(rs.getObject("expires_at", OffsetDateTime.class));
        token.setRevokedAt(rs.getObject("revoked_at", OffsetDateTime.class));
        token.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return token;
    }

    private PublicShareExpenseItem mapPublicExpense(ResultSet rs, int rowNum) throws SQLException {
        return new PublicShareExpenseItem(
            rs.getString("description"),
            rs.getBigDecimal("amount"),
            rs.getObject("expense_date", LocalDate.class),
            rs.getObject("category_id", UUID.class),
            rs.getString("category_name"),
            rs.getString("category_icon"),
            rs.getString("category_color_hex"),
            rs.getString("payment_method"),
            rs.getBoolean("is_fixed")
        );
    }
}
