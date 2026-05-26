package com.hermes.finance.domain.category;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ExpenseCategoryRowMapper implements RowMapper<ExpenseCategory> {
    @Override
    public ExpenseCategory mapRow(ResultSet rs, int rowNum) throws SQLException {
        ExpenseCategory category = new ExpenseCategory();
        category.setId(rs.getObject("id", java.util.UUID.class));
        category.setUserId(rs.getObject("user_id", java.util.UUID.class));
        category.setName(rs.getString("name"));
        category.setIcon(rs.getString("icon"));
        category.setColorHex(rs.getString("color_hex"));
        category.setDefault(rs.getBoolean("is_default"));
        category.setActive(rs.getBoolean("active"));
        category.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        return category;
    }
}
