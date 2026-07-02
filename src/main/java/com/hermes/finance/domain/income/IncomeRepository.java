package com.hermes.finance.domain.income;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class IncomeRepository implements IncomeRepositoryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NativeQueryCatalog nativeQueryCatalog;
    private final IncomeRowMapper rowMapper;

    public IncomeRepository(NamedParameterJdbcTemplate jdbcTemplate,
                            NativeQueryCatalog nativeQueryCatalog,
                            IncomeRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.nativeQueryCatalog = nativeQueryCatalog;
        this.rowMapper = rowMapper;
    }

    @Override
    public List<Income> findByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        String sql = nativeQueryCatalog.get("income.findByUserAndPeriod");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("startDate", startDate)
            .addValue("endDate", endDate);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public Optional<Income> findById(UUID id) {
        String sql = nativeQueryCatalog.get("income.findById");
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), rowMapper));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Income save(Income income) {
        UUID id = income.getId() != null ? income.getId() : UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        String sql = nativeQueryCatalog.get("income.insert");
        MapSqlParameterSource params = baseParams(income)
            .addValue("id", id)
            .addValue("createdAt", now)
            .addValue("updatedAt", now);

        Income saved = jdbcTemplate.queryForObject(sql, params, rowMapper);
        if (saved == null) {
            throw new IllegalStateException("Income insert did not return a row");
        }
        return saved;
    }

    @Override
    public Optional<Income> update(Income income) {
        String sql = nativeQueryCatalog.get("income.update");
        MapSqlParameterSource params = baseParams(income)
            .addValue("id", income.getId())
            .addValue("updatedAt", OffsetDateTime.now());
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(UUID id, UUID userId) {
        String sql = nativeQueryCatalog.get("income.delete");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean categoryIsAccessible(UUID categoryId, UUID userId) {
        String sql = nativeQueryCatalog.get("income.categoryIsAccessible");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("categoryId", categoryId)
            .addValue("userId", userId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    private MapSqlParameterSource baseParams(Income income) {
        return new MapSqlParameterSource()
            .addValue("userId", income.getUserId())
            .addValue("categoryId", income.getCategoryId())
            .addValue("description", income.getDescription())
            .addValue("amount", income.getAmount())
            .addValue("incomeDate", income.getIncomeDate())
            .addValue("isRecurring", income.isRecurring())
            .addValue("notes", income.getNotes());
    }
}
