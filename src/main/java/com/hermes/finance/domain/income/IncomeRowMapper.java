package com.hermes.finance.domain.income;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class IncomeRowMapper implements RowMapper<Income> {
    @Override
    public Income mapRow(ResultSet rs, int rowNum) throws SQLException {
        Income income = new Income();
        income.setId(rs.getObject("id", java.util.UUID.class));
        income.setUserId(rs.getObject("user_id", java.util.UUID.class));
        income.setCategoryId(rs.getObject("category_id", java.util.UUID.class));
        income.setDescription(rs.getString("description"));
        income.setAmount(rs.getBigDecimal("amount"));
        income.setIncomeDate(rs.getObject("income_date", java.time.LocalDate.class));
        income.setRecurring(rs.getBoolean("is_recurring"));
        income.setNotes(rs.getString("notes"));
        income.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        income.setUpdatedAt(rs.getObject("updated_at", java.time.OffsetDateTime.class));
        return income;
    }
}
