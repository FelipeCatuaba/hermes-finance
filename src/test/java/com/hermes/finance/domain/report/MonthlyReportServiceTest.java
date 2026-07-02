package com.hermes.finance.domain.report;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.response.MonthlyReportResponse;
import com.hermes.finance.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportServiceTest {

    private static final UUID USER_ID = UUID.fromString("0f35ce81-2f92-4547-baf5-9011cb879413");
    private static final UUID INCOME_ID = UUID.fromString("d6843534-1864-44e3-a9e6-1f8b6d741f5e");
    private static final UUID CATEGORY_ID = UUID.fromString("2c34ed5d-8d9f-4e7f-87e3-a34be90d60cb");
    private static final UUID MEMBER_ID = UUID.fromString("8bd15f7b-6cf3-4a95-8bc7-d244807e4620");

    @Mock
    private MonthlyReportRepositoryPort repository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private MonthlyReportService service;

    @Test
    void shouldBuildMonthlyReportWithoutMixingFamilyExpensesIntoOwnerMetrics() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 1);
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.sumIncome(USER_ID, startDate, endDate)).thenReturn(new BigDecimal("1000.00"));
        when(repository.sumOwnerExpenses(USER_ID, startDate, endDate)).thenReturn(new BigDecimal("250.00"));
        when(repository.sumFamilyExpenses(USER_ID, startDate, endDate)).thenReturn(new BigDecimal("300.00"));
        when(repository.findIncomeItems(USER_ID, startDate, endDate)).thenReturn(List.of(new MonthlyIncomeItem(
            INCOME_ID,
            "Salario",
            new BigDecimal("1000.00"),
            LocalDate.of(2026, 3, 5),
            CATEGORY_ID,
            "Salario",
            "wallet",
            false
        )));
        when(repository.sumOwnerExpensesByCategory(USER_ID, startDate, endDate)).thenReturn(List.of(new MonthlyCategoryExpense(
            CATEGORY_ID,
            "Mercado",
            "cart",
            "#ff5577",
            new BigDecimal("250.00")
        )));
        when(repository.sumFamilyExpensesByMember(USER_ID, startDate, endDate)).thenReturn(List.of(new MonthlyFamilyExpense(
            MEMBER_ID,
            "Isa",
            "Filha",
            new BigDecimal("300.00")
        )));

        MonthlyReportResponse response = service.getMonthlyReport(3, 2026);

        assertEquals(new BigDecimal("1000.00"), response.income().total());
        assertEquals(new BigDecimal("250.00"), response.ownerExpenses().total());
        assertEquals(new BigDecimal("300.00"), response.familyExpenses().total());
        assertEquals(new BigDecimal("550.00"), response.summary().totalExpenses());
        assertEquals(new BigDecimal("750.00"), response.summary().ownerBalance());
        assertEquals(new BigDecimal("75.00"), response.summary().savingsRate());
        assertEquals(new BigDecimal("25.00"), response.ownerExpenses().byCategory().get(0).pctOfIncome());
        assertEquals("Isa", response.familyExpenses().byMember().get(0).familyMember().name());
    }

    @Test
    void shouldReturnNullPercentagesWhenIncomeIsZero() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 1);
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.sumIncome(USER_ID, startDate, endDate)).thenReturn(BigDecimal.ZERO);
        when(repository.sumOwnerExpenses(USER_ID, startDate, endDate)).thenReturn(new BigDecimal("250.00"));
        when(repository.sumFamilyExpenses(USER_ID, startDate, endDate)).thenReturn(BigDecimal.ZERO);
        when(repository.findIncomeItems(USER_ID, startDate, endDate)).thenReturn(List.of());
        when(repository.sumOwnerExpensesByCategory(USER_ID, startDate, endDate)).thenReturn(List.of(new MonthlyCategoryExpense(
            CATEGORY_ID,
            "Mercado",
            "cart",
            "#ff5577",
            new BigDecimal("250.00")
        )));
        when(repository.sumFamilyExpensesByMember(USER_ID, startDate, endDate)).thenReturn(List.of());

        MonthlyReportResponse response = service.getMonthlyReport(3, 2026);

        assertNull(response.ownerExpenses().byCategory().get(0).pctOfIncome());
        assertNull(response.summary().savingsRate());
        assertEquals(new BigDecimal("-250.00"), response.summary().ownerBalance());
    }

    @Test
    void shouldRejectInvalidPeriod() {
        ResponseStatusException invalidMonth = assertThrows(ResponseStatusException.class, () -> service.getMonthlyReport(13, 2026));
        ResponseStatusException invalidYear = assertThrows(ResponseStatusException.class, () -> service.getMonthlyReport(3, 1899));

        assertEquals(HttpStatus.BAD_REQUEST, invalidMonth.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, invalidYear.getStatusCode());
    }

    private User user() {
        User user = new User();
        user.setId(USER_ID);
        return user;
    }
}
