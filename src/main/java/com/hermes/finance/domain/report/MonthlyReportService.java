package com.hermes.finance.domain.report;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.response.ExpenseCategorySummaryResponse;
import com.hermes.finance.dto.response.ExpenseFamilyMemberSummaryResponse;
import com.hermes.finance.dto.response.MonthlyReportResponse;
import com.hermes.finance.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class MonthlyReportService {

    private final MonthlyReportRepositoryPort repository;
    private final SecurityUtils securityUtils;

    public MonthlyReportService(MonthlyReportRepositoryPort repository, SecurityUtils securityUtils) {
        this.repository = repository;
        this.securityUtils = securityUtils;
    }

    public MonthlyReportResponse getMonthlyReport(int month, int year) {
        YearMonth period = validatePeriod(month, year);
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.plusMonths(1).atDay(1);
        User currentUser = securityUtils.getCurrentUser();

        BigDecimal incomeTotal = repository.sumIncome(currentUser.getId(), startDate, endDate);
        BigDecimal ownerExpensesTotal = repository.sumOwnerExpenses(currentUser.getId(), startDate, endDate);
        BigDecimal familyExpensesTotal = repository.sumFamilyExpenses(currentUser.getId(), startDate, endDate);

        List<MonthlyReportResponse.IncomeItem> incomeItems = repository.findIncomeItems(currentUser.getId(), startDate, endDate)
            .stream()
            .map(this::toIncomeItem)
            .toList();
        List<MonthlyReportResponse.CategoryBreakdown> ownerByCategory = repository.sumOwnerExpensesByCategory(currentUser.getId(), startDate, endDate)
            .stream()
            .map(item -> toCategoryBreakdown(item, incomeTotal))
            .toList();
        List<MonthlyReportResponse.MemberBreakdown> familyByMember = repository.sumFamilyExpensesByMember(currentUser.getId(), startDate, endDate)
            .stream()
            .map(this::toMemberBreakdown)
            .toList();

        BigDecimal totalExpenses = ownerExpensesTotal.add(familyExpensesTotal);
        BigDecimal ownerBalance = incomeTotal.subtract(ownerExpensesTotal);
        BigDecimal savingsRate = percentage(ownerBalance, incomeTotal);

        return new MonthlyReportResponse(
            new MonthlyReportResponse.IncomeSection(incomeTotal, incomeItems),
            new MonthlyReportResponse.OwnerExpensesSection(ownerExpensesTotal, ownerByCategory),
            new MonthlyReportResponse.FamilyExpensesSection(familyExpensesTotal, familyByMember),
            new MonthlyReportResponse.Summary(totalExpenses, ownerBalance, savingsRate)
        );
    }

    private YearMonth validatePeriod(int month, int year) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mes invalido");
        }
        if (year < 1900 || year > 9999) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ano invalido");
        }
        return YearMonth.of(year, month);
    }

    private MonthlyReportResponse.IncomeItem toIncomeItem(MonthlyIncomeItem item) {
        ExpenseCategorySummaryResponse category = item.categoryId() == null ? null : new ExpenseCategorySummaryResponse(
            item.categoryId(),
            item.categoryName(),
            item.categoryIcon(),
            null
        );
        return new MonthlyReportResponse.IncomeItem(
            item.id(),
            item.description(),
            item.amount(),
            item.incomeDate(),
            category,
            item.recurring()
        );
    }

    private MonthlyReportResponse.CategoryBreakdown toCategoryBreakdown(MonthlyCategoryExpense item, BigDecimal incomeTotal) {
        ExpenseCategorySummaryResponse category = item.categoryId() == null ? null : new ExpenseCategorySummaryResponse(
            item.categoryId(),
            item.categoryName(),
            item.categoryIcon(),
            item.categoryColorHex()
        );
        return new MonthlyReportResponse.CategoryBreakdown(category, item.total(), percentage(item.total(), incomeTotal));
    }

    private MonthlyReportResponse.MemberBreakdown toMemberBreakdown(MonthlyFamilyExpense item) {
        ExpenseFamilyMemberSummaryResponse member = item.familyMemberId() == null ? null : new ExpenseFamilyMemberSummaryResponse(
            item.familyMemberId(),
            item.familyMemberName(),
            item.familyMemberRelation()
        );
        return new MonthlyReportResponse.MemberBreakdown(member, item.total());
    }

    private BigDecimal percentage(BigDecimal amount, BigDecimal base) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return amount.multiply(new BigDecimal("100")).divide(base, 2, RoundingMode.HALF_UP);
    }
}
