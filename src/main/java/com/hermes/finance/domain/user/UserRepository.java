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
        OffsetDateTime updatedAt = now;

        String sql = nativeQueryCatalog.get("user.insert");

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("email", user.getEmail())
            .addValue("name", user.getName())
            .addValue("password", user.getPassword())
            .addValue("role", user.getRole())
            .addValue("active", user.isActive())
            .addValue("createdAt", createdAt)
            .addValue("updatedAt", updatedAt);

        jdbcTemplate.update(sql, params);
        user.setId(id);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);
        return user;
    }

    public Optional<User> findByEmail(String email) {
        String sql = nativeQueryCatalog.get("user.findByEmail");

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("email", email);

        return jdbcTemplate.query(sql, params, userRowMapper).stream().findFirst();
    }

    public Optional<User> findById(UUID id) {
        String sql = nativeQueryCatalog.get("user.findById");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id);
        return jdbcTemplate.query(sql, params, userRowMapper).stream().findFirst();
    }

    public void updatePassword(UUID id, String password) {
        String sql = nativeQueryCatalog.get("user.updatePassword");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("password", password)
            .addValue("updatedAt", OffsetDateTime.now());
        jdbcTemplate.update(sql, params);
    }
}
