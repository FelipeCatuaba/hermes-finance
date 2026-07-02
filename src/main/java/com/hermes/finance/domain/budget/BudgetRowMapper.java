package com.hermes.finance.domain.budget;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class BudgetRowMapper implements RowMapper<Budget> {
    @Override
    public Budget mapRow(ResultSet rs, int rowNum) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getObject("id", UUID.class));
        budget.setUserId(rs.getObject("user_id", UUID.class));
        budget.setCategoryId(rs.getObject("category_id", UUID.class));
        budget.setMonth(rs.getInt("month"));
        budget.setYear(rs.getInt("year"));
        budget.setAmountLimit(rs.getBigDecimal("amount_limit"));
        budget.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return budget;
    }
}
