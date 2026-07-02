package com.hermes.finance.domain.expense;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ExpenseRowMapper implements RowMapper<Expense> {
    @Override
    public Expense mapRow(ResultSet rs, int rowNum) throws SQLException {
        Expense expense = new Expense();
        expense.setId(rs.getObject("id", java.util.UUID.class));
        expense.setUserId(rs.getObject("user_id", java.util.UUID.class));
        expense.setFamilyMemberId(rs.getObject("family_member_id", java.util.UUID.class));
        expense.setCategoryId(rs.getObject("category_id", java.util.UUID.class));
        expense.setInstallmentGroupId(rs.getObject("installment_group_id", java.util.UUID.class));
        expense.setDescription(rs.getString("description"));
        expense.setAmount(rs.getBigDecimal("amount"));
        expense.setExpenseDate(rs.getObject("expense_date", java.time.LocalDate.class));
        Number installmentNumber = (Number) rs.getObject("installment_number");
        Number totalInstallments = (Number) rs.getObject("total_installments");
        expense.setInstallmentNumber(installmentNumber == null ? null : installmentNumber.intValue());
        expense.setTotalInstallments(totalInstallments == null ? null : totalInstallments.intValue());
        expense.setRecurring(rs.getBoolean("is_recurring"));
        expense.setFixed(rs.getBoolean("is_fixed"));
        expense.setPaymentMethod(rs.getString("payment_method"));
        expense.setNotes(rs.getString("notes"));
        expense.setScope(rs.getString("scope"));
        expense.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        expense.setUpdatedAt(rs.getObject("updated_at", java.time.OffsetDateTime.class));
        return expense;
    }
}
