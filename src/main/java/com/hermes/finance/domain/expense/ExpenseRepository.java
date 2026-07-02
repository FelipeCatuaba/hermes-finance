package com.hermes.finance.domain.expense;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public class ExpenseRepository implements ExpenseRepositoryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NativeQueryCatalog nativeQueryCatalog;
    private final ExpenseRowMapper rowMapper;

    public ExpenseRepository(NamedParameterJdbcTemplate jdbcTemplate,
                             NativeQueryCatalog nativeQueryCatalog,
                             ExpenseRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.nativeQueryCatalog = nativeQueryCatalog;
        this.rowMapper = rowMapper;
    }

    @Override
    public Expense save(Expense expense) {
        UUID id = expense.getId() != null ? expense.getId() : UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        String sql = nativeQueryCatalog.get("expense.insert");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", expense.getUserId())
            .addValue("familyMemberId", expense.getFamilyMemberId())
            .addValue("categoryId", expense.getCategoryId())
            .addValue("installmentGroupId", expense.getInstallmentGroupId())
            .addValue("description", expense.getDescription())
            .addValue("amount", expense.getAmount())
            .addValue("expenseDate", expense.getExpenseDate())
            .addValue("installmentNumber", expense.getInstallmentNumber())
            .addValue("totalInstallments", expense.getTotalInstallments())
            .addValue("isRecurring", expense.isRecurring())
            .addValue("isFixed", expense.isFixed())
            .addValue("paymentMethod", expense.getPaymentMethod())
            .addValue("notes", expense.getNotes())
            .addValue("scope", expense.getScope())
            .addValue("createdAt", now)
            .addValue("updatedAt", now);

        Expense saved = jdbcTemplate.queryForObject(sql, params, rowMapper);
        if (saved == null) {
            throw new IllegalStateException("Expense insert did not return a row");
        }
        return saved;
    }

    @Override
    public UUID createInstallmentGroup(UUID userId,
                                       String description,
                                       java.math.BigDecimal totalAmount,
                                       int totalInstallments,
                                       java.time.LocalDate firstDueDate) {
        UUID id = UUID.randomUUID();
        String sql = nativeQueryCatalog.get("installmentGroup.insert");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", userId)
            .addValue("description", description)
            .addValue("totalAmount", totalAmount)
            .addValue("totalInstallments", totalInstallments)
            .addValue("firstDueDate", firstDueDate)
            .addValue("createdAt", OffsetDateTime.now());
        UUID savedId = jdbcTemplate.queryForObject(sql, params, UUID.class);
        if (savedId == null) {
            throw new IllegalStateException("Installment group insert did not return an id");
        }
        return savedId;
    }

    @Override
    public boolean familyMemberBelongsToUser(UUID familyMemberId, UUID userId) {
        String sql = nativeQueryCatalog.get("expense.familyMemberBelongsToUser");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("familyMemberId", familyMemberId)
            .addValue("userId", userId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean categoryIsAccessible(UUID categoryId, UUID userId) {
        String sql = nativeQueryCatalog.get("expense.categoryIsAccessible");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("categoryId", categoryId)
            .addValue("userId", userId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}
