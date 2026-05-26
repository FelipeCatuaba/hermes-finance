package com.hermes.finance.domain.family;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FamilyMemberRowMapper implements RowMapper<FamilyMember> {
    @Override
    public FamilyMember mapRow(ResultSet rs, int rowNum) throws SQLException {
        FamilyMember member = new FamilyMember();
        member.setId(rs.getObject("id", java.util.UUID.class));
        member.setUserId(rs.getObject("user_id", java.util.UUID.class));
        member.setName(rs.getString("name"));
        member.setRelation(rs.getString("relation"));
        member.setActive(rs.getBoolean("active"));
        member.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        return member;
    }
}
