package com.hermes.finance.domain.family;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FamilyMemberRepository implements FamilyMemberRepositoryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NativeQueryCatalog nativeQueryCatalog;
    private final FamilyMemberRowMapper rowMapper;

    public FamilyMemberRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                  NativeQueryCatalog nativeQueryCatalog,
                                  FamilyMemberRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.nativeQueryCatalog = nativeQueryCatalog;
        this.rowMapper = rowMapper;
    }

    @Override
    public FamilyMember save(FamilyMember member) {
        UUID id = member.getId() != null ? member.getId() : UUID.randomUUID();
        OffsetDateTime createdAt = member.getCreatedAt() != null ? member.getCreatedAt() : OffsetDateTime.now();

        String sql = nativeQueryCatalog.get("familyMember.insert");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", member.getUserId())
            .addValue("name", member.getName())
            .addValue("relation", member.getRelation())
            .addValue("active", member.isActive())
            .addValue("createdAt", createdAt);

        jdbcTemplate.update(sql, params);
        member.setId(id);
        member.setCreatedAt(createdAt);
        return member;
    }

    @Override
    public List<FamilyMember> findByUserId(UUID userId, boolean includeInactive) {
        String sql = nativeQueryCatalog.get("familyMember.findByUserId");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("includeInactive", includeInactive);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public Optional<FamilyMember> findById(UUID id) {
        String sql = nativeQueryCatalog.get("familyMember.findById");
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        return jdbcTemplate.query(sql, params, rowMapper).stream().findFirst();
    }

    @Override
    public FamilyMember update(FamilyMember member) {
        String sql = nativeQueryCatalog.get("familyMember.update");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", member.getId())
            .addValue("name", member.getName())
            .addValue("relation", member.getRelation())
            .addValue("active", member.isActive());

        jdbcTemplate.update(sql, params);
        return member;
    }

    @Override
    public void deactivate(UUID id) {
        String sql = nativeQueryCatalog.get("familyMember.deactivate");
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update(sql, params);
    }
}
