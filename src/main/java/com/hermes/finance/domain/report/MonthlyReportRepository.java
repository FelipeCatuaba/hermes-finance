package com.hermes.finance.domain.report;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class MonthlyReportRepository implements MonthlyReportRepositoryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NativeQueryCatalog nativeQueryCatalog;

    public MonthlyReportRepository(NamedParameterJdbcTemplate jdbcTemplate, NativeQueryCatalog nativeQueryCatalog) {
        this.jdbcTemplate = jdbcTemplate;
        this.nativeQueryCatalog = nativeQueryCatalog;
    }

    @Override
    public List<MonthlyIncomeItem> findIncomeItems(UUID userId, LocalDate startDate, LocalDate endDate) {
        String sql = nativeQueryCatalog.get("monthlyReport.incomeItems");
        return jdbcTemplate.query(sql, periodParams(userId, startDate, endDate), this::mapIncomeItem);
    }

    @Override
    public BigDecimal sumIncome(UUID userId, LocalDate startDate, LocalDate endDate) {
        return queryTotal("monthlyReport.incomeTotal", userId, startDate, endDate);
    }

    @Override
    public List<MonthlyCategoryExpense> sumOwnerExpensesByCategory(UUID userId, LocalDate startDate, LocalDate endDate) {
        String sql = nativeQueryCatalog.get("monthlyReport.ownerExpensesByCategory");
        return jdbcTemplate.query(sql, periodParams(userId, startDate, endDate), this::mapCategoryExpense);
    }

    @Override
    public BigDecimal sumOwnerExpenses(UUID userId, LocalDate startDate, LocalDate endDate) {
        return queryTotal("monthlyReport.ownerExpensesTotal", userId, startDate, endDate);
    }

    @Override
    public List<MonthlyFamilyExpense> sumFamilyExpensesByMember(UUID userId, LocalDate startDate, LocalDate endDate) {
        String sql = nativeQueryCatalog.get("monthlyReport.familyExpensesByMember");
        return jdbcTemplate.query(sql, periodParams(userId, startDate, endDate), this::mapFamilyExpense);
    }

    @Override
    public BigDecimal sumFamilyExpenses(UUID userId, LocalDate startDate, LocalDate endDate) {
        return queryTotal("monthlyReport.familyExpensesTotal", userId, startDate, endDate);
    }

    private BigDecimal queryTotal(String queryName, UUID userId, LocalDate startDate, LocalDate endDate) {
        String sql = nativeQueryCatalog.get(queryName);
        BigDecimal total = jdbcTemplate.queryForObject(sql, periodParams(userId, startDate, endDate), BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    private MapSqlParameterSource periodParams(UUID userId, LocalDate startDate, LocalDate endDate) {
        return new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("startDate", startDate)
            .addValue("endDate", endDate);
    }

    private MonthlyIncomeItem mapIncomeItem(ResultSet rs, int rowNum) throws SQLException {
        return new MonthlyIncomeItem(
            rs.getObject("id", UUID.class),
            rs.getString("description"),
            rs.getBigDecimal("amount"),
            rs.getObject("income_date", LocalDate.class),
            rs.getObject("category_id", UUID.class),
            rs.getString("category_name"),
            rs.getString("category_icon"),
            rs.getBoolean("is_recurring")
        );
    }

    private MonthlyCategoryExpense mapCategoryExpense(ResultSet rs, int rowNum) throws SQLException {
        return new MonthlyCategoryExpense(
            rs.getObject("category_id", UUID.class),
            rs.getString("category_name"),
            rs.getString("category_icon"),
            rs.getString("category_color_hex"),
            rs.getBigDecimal("total")
        );
    }

    private MonthlyFamilyExpense mapFamilyExpense(ResultSet rs, int rowNum) throws SQLException {
        return new MonthlyFamilyExpense(
            rs.getObject("family_member_id", UUID.class),
            rs.getString("family_member_name"),
            rs.getString("family_member_relation"),
            rs.getBigDecimal("total")
        );
    }
}
