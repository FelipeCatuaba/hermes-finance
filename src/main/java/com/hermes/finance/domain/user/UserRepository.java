package com.hermes.finance.domain.user;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NativeQueryCatalog nativeQueryCatalog;
    private final UserRowMapper userRowMapper;

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate,
                          NativeQueryCatalog nativeQueryCatalog,
                          UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.nativeQueryCatalog = nativeQueryCatalog;
        this.userRowMapper = userRowMapper;
    }

    public User save(User user) {
        UUID id = user.getId() != null ? user.getId() : UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime createdAt = user.getCreatedAt() != null ? user.getCreatedAt() : now;

        String sql = nativeQueryCatalog.get("user.insert");

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("clerkId", user.getClerkId())
            .addValue("email", user.getEmail())
            .addValue("name", user.getName())
            .addValue("createdAt", createdAt)
            .addValue("updatedAt", now);

        jdbcTemplate.update(sql, params);
        user.setId(id);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(now);
        return user;
    }

    public Optional<User> findByClerkId(String clerkId) {
        String sql = nativeQueryCatalog.get("user.findByClerkId");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("clerkId", clerkId);
        return jdbcTemplate.query(sql, params, userRowMapper).stream().findFirst();
    }

    public Optional<User> findById(UUID id) {
        String sql = nativeQueryCatalog.get("user.findById");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id);
        return jdbcTemplate.query(sql, params, userRowMapper).stream().findFirst();
    }

    public void updateProfile(String clerkId, String email, String name) {
        String sql = nativeQueryCatalog.get("user.updateProfile");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("clerkId", clerkId)
            .addValue("email", email)
            .addValue("name", name)
            .addValue("updatedAt", OffsetDateTime.now());
        jdbcTemplate.update(sql, params);
    }

    public void anonymize(String clerkId) {
        String sql = nativeQueryCatalog.get("user.anonymize");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("clerkId", clerkId)
            .addValue("updatedAt", OffsetDateTime.now());
        jdbcTemplate.update(sql, params);
    }
}
