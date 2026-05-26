package com.hermes.finance.domain.category;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ExpenseCategoryRepository implements ExpenseCategoryRepositoryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NativeQueryCatalog nativeQueryCatalog;
    private final ExpenseCategoryRowMapper rowMapper;

    public ExpenseCategoryRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                     NativeQueryCatalog nativeQueryCatalog,
                                     ExpenseCategoryRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.nativeQueryCatalog = nativeQueryCatalog;
        this.rowMapper = rowMapper;
    }

    @Override
    public ExpenseCategory save(ExpenseCategory category) {
        UUID id = category.getId() != null ? category.getId() : UUID.randomUUID();
        OffsetDateTime createdAt = category.getCreatedAt() != null ? category.getCreatedAt() : OffsetDateTime.now();

        String sql = nativeQueryCatalog.get("expenseCategory.insert");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", category.getUserId())
            .addValue("name", category.getName())
            .addValue("icon", category.getIcon())
            .addValue("colorHex", category.getColorHex())
            .addValue("isDefault", category.isDefault())
            .addValue("active", category.isActive())
            .addValue("createdAt", createdAt);

        jdbcTemplate.update(sql, params);
        category.setId(id);
        category.setCreatedAt(createdAt);
        return category;
    }

    @Override
    public List<ExpenseCategory> findVisibleForUser(UUID userId, boolean includeInactive) {
        String sql = nativeQueryCatalog.get("expenseCategory.findVisibleForUser");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("includeInactive", includeInactive);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public Optional<ExpenseCategory> findById(UUID id) {
        String sql = nativeQueryCatalog.get("expenseCategory.findById");
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        return jdbcTemplate.query(sql, params, rowMapper).stream().findFirst();
    }

    @Override
    public ExpenseCategory update(ExpenseCategory category) {
        String sql = nativeQueryCatalog.get("expenseCategory.update");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", category.getId())
            .addValue("name", category.getName())
            .addValue("icon", category.getIcon())
            .addValue("colorHex", category.getColorHex())
            .addValue("active", category.isActive());

        jdbcTemplate.update(sql, params);
        return category;
    }

    @Override
    public void deleteById(UUID id) {
        String sql = nativeQueryCatalog.get("expenseCategory.delete");
        jdbcTemplate.update(sql, new MapSqlParameterSource().addValue("id", id));
    }

    @Override
    public void deactivate(UUID id) {
        String sql = nativeQueryCatalog.get("expenseCategory.deactivate");
        jdbcTemplate.update(sql, new MapSqlParameterSource().addValue("id", id));
    }

    @Override
    public boolean isCategoryInUse(UUID id) {
        String sql = nativeQueryCatalog.get("expenseCategory.isInUse");
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource().addValue("id", id), Integer.class);
        return count != null && count > 0;
    }
}
