package com.hermes.finance.domain.expense;

import com.hermes.finance.util.NativeQueryCatalog;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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
    public Optional<Expense> findById(UUID id) {
        String sql = nativeQueryCatalog.get("expense.findById");
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), rowMapper));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<ExpenseListItem> findByUserAndPeriod(UUID userId,
                                                     LocalDate startDate,
                                                     LocalDate endDate,
                                                     UUID categoryId,
                                                     UUID familyMemberId,
                                                     int limit,
                                                     int offset) {
        String sql = nativeQueryCatalog.get("expense.findByUserAndPeriod");
        return jdbcTemplate.query(sql, listParams(userId, startDate, endDate, categoryId, familyMemberId)
            .addValue("limit", limit)
            .addValue("offset", offset), this::mapListItem);
    }

    @Override
    public long countByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID familyMemberId) {
        String sql = nativeQueryCatalog.get("expense.countByUserAndPeriod");
        Long total = jdbcTemplate.queryForObject(sql, listParams(userId, startDate, endDate, categoryId, familyMemberId), Long.class);
        return total == null ? 0 : total;
    }

    @Override
    public BigDecimal sumByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID familyMemberId) {
        String sql = nativeQueryCatalog.get("expense.sumByUserAndPeriod");
        BigDecimal total = jdbcTemplate.queryForObject(sql, listParams(userId, startDate, endDate, categoryId, familyMemberId), BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
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
    public Optional<Expense> update(Expense expense) {
        String sql = nativeQueryCatalog.get("expense.update");
        MapSqlParameterSource params = baseParams(expense)
            .addValue("id", expense.getId())
            .addValue("updatedAt", OffsetDateTime.now());
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(UUID id, UUID userId) {
        String sql = nativeQueryCatalog.get("expense.delete");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
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
    public Optional<UUID> findInstallmentGroupUserId(UUID id) {
        String sql = nativeQueryCatalog.get("installmentGroup.findUserIdById");
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), UUID.class));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteExpensesByInstallmentGroup(UUID id, UUID userId) {
        String sql = nativeQueryCatalog.get("installmentGroup.deleteExpenses");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void deleteInstallmentGroup(UUID id, UUID userId) {
        String sql = nativeQueryCatalog.get("installmentGroup.delete");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
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

    private MapSqlParameterSource baseParams(Expense expense) {
        return new MapSqlParameterSource()
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
            .addValue("scope", expense.getScope());
    }

    private MapSqlParameterSource listParams(UUID userId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID familyMemberId) {
        return new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("startDate", startDate)
            .addValue("endDate", endDate)
            .addValue("categoryId", categoryId)
            .addValue("familyMemberId", familyMemberId);
    }

    private ExpenseListItem mapListItem(ResultSet rs, int rowNum) throws SQLException {
        Number installmentNumber = (Number) rs.getObject("installment_number");
        Number totalInstallments = (Number) rs.getObject("total_installments");
        return new ExpenseListItem(
            rs.getObject("id", UUID.class),
            rs.getString("description"),
            rs.getBigDecimal("amount"),
            rs.getObject("expense_date", LocalDate.class),
            rs.getObject("category_id", UUID.class),
            rs.getString("category_name"),
            rs.getString("category_icon"),
            rs.getString("category_color_hex"),
            rs.getObject("family_member_id", UUID.class),
            rs.getString("family_member_name"),
            rs.getString("family_member_relation"),
            rs.getObject("installment_group_id", UUID.class),
            installmentNumber == null ? null : installmentNumber.intValue(),
            totalInstallments == null ? null : totalInstallments.intValue(),
            rs.getString("payment_method"),
            rs.getString("notes"),
            rs.getBoolean("is_fixed"),
            rs.getString("scope"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
        );
    }
}
