package com.hermes.finance.domain.budget;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BudgetRepository implements BudgetRepositoryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NativeQueryCatalog nativeQueryCatalog;
    private final BudgetRowMapper rowMapper;

    public BudgetRepository(NamedParameterJdbcTemplate jdbcTemplate,
                            NativeQueryCatalog nativeQueryCatalog,
                            BudgetRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.nativeQueryCatalog = nativeQueryCatalog;
        this.rowMapper = rowMapper;
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        String sql = nativeQueryCatalog.get("budget.findById");
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), rowMapper));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<Budget> findByUserAndPeriod(UUID userId, int month, int year) {
        String sql = nativeQueryCatalog.get("budget.findByUserAndPeriod");
        return jdbcTemplate.query(sql, periodParams(userId, month, year), rowMapper);
    }

    @Override
    public Budget upsert(Budget budget) {
        String sql = nativeQueryCatalog.get("budget.upsert");
        Budget saved = jdbcTemplate.queryForObject(sql, budgetParams(budget), rowMapper);
        if (saved == null) {
            throw new IllegalStateException("Budget upsert did not return a row");
        }
        return saved;
    }

    @Override
    public Optional<Budget> update(Budget budget) {
        String sql = nativeQueryCatalog.get("budget.update");
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, budgetParams(budget).addValue("id", budget.getId()), rowMapper));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(UUID id, UUID userId) {
        String sql = nativeQueryCatalog.get("budget.delete");
        jdbcTemplate.update(sql, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", userId));
    }

    @Override
    public boolean categoryIsAccessible(UUID categoryId, UUID userId) {
        String sql = nativeQueryCatalog.get("budget.categoryIsAccessible");
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource()
            .addValue("categoryId", categoryId)
            .addValue("userId", userId), Integer.class);
        return count != null && count > 0;
    }

    @Override
    public List<BudgetStatusItem> findStatus(UUID userId, int month, int year, LocalDate startDate, LocalDate endDate) {
        String sql = nativeQueryCatalog.get("budget.status");
        return jdbcTemplate.query(sql, periodParams(userId, month, year)
            .addValue("startDate", startDate)
            .addValue("endDate", endDate), this::mapStatusItem);
    }

    @Override
    public void copyPreviousMonth(UUID userId, int fromMonth, int fromYear, int toMonth, int toYear) {
        String sql = nativeQueryCatalog.get("budget.copyPreviousMonth");
        jdbcTemplate.update(sql, new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("fromMonth", fromMonth)
            .addValue("fromYear", fromYear)
            .addValue("toMonth", toMonth)
            .addValue("toYear", toYear));
    }

    private MapSqlParameterSource budgetParams(Budget budget) {
        return new MapSqlParameterSource()
            .addValue("userId", budget.getUserId())
            .addValue("categoryId", budget.getCategoryId())
            .addValue("month", budget.getMonth())
            .addValue("year", budget.getYear())
            .addValue("amountLimit", budget.getAmountLimit());
    }

    private MapSqlParameterSource periodParams(UUID userId, int month, int year) {
        return new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("month", month)
            .addValue("year", year);
    }

    private BudgetStatusItem mapStatusItem(ResultSet rs, int rowNum) throws SQLException {
        return new BudgetStatusItem(
            rs.getObject("category_id", UUID.class),
            rs.getString("category_name"),
            rs.getString("category_icon"),
            rs.getString("category_color_hex"),
            rs.getObject("budget_id", UUID.class),
            rs.getBigDecimal("amount_limit"),
            rs.getBigDecimal("spent_amount")
        );
    }
}
