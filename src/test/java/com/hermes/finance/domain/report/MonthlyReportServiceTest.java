package com.hermes.finance.domain.report;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.response.MonthlyReportResponse;
import com.hermes.finance.dto.response.OpenInstallmentsReportResponse;
import com.hermes.finance.dto.response.YearlyReportResponse;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MonthlyReportServiceTest {

    private static final UUID USER_ID = UUID.fromString("0f35ce81-2f92-4547-baf5-9011cb879413");
    private static final UUID INCOME_ID = UUID.fromString("d6843534-1864-44e3-a9e6-1f8b6d741f5e");
    private static final UUID CATEGORY_ID = UUID.fromString("2c34ed5d-8d9f-4e7f-87e3-a34be90d60cb");
    private static final UUID MEMBER_ID = UUID.fromString("8bd15f7b-6cf3-4a95-8bc7-d244807e4620");
    private static final UUID GROUP_ID = UUID.fromString("6fcd1116-0357-4ba3-9617-ac65d32a054b");
    private static final UUID SECOND_GROUP_ID = UUID.fromString("5c8a530e-76da-4092-bf4d-c5e6a5d15293");
    private static final UUID INSTALLMENT_ID = UUID.fromString("34ca51c7-840f-4b14-b9fd-9ed78f44e9a9");
    private static final UUID SECOND_INSTALLMENT_ID = UUID.fromString("d6be30e1-06b5-4267-8ce4-1938f0f66f3c");

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

    @Test
    void shouldBuildYearlyReportWithAllMonthsAndEmptyMonthsZeroed() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2027, 1, 1);
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.sumIncomeByMonth(USER_ID, startDate, endDate)).thenReturn(List.of(
            new YearlyReportAmount(1, new BigDecimal("1000.00")),
            new YearlyReportAmount(3, new BigDecimal("2000.00"))
        ));
        when(repository.sumOwnerExpensesByMonth(USER_ID, startDate, endDate)).thenReturn(List.of(
            new YearlyReportAmount(1, new BigDecimal("250.00"))
        ));
        when(repository.sumFamilyExpensesByMonth(USER_ID, startDate, endDate)).thenReturn(List.of(
            new YearlyReportAmount(3, new BigDecimal("400.00"))
        ));

        YearlyReportResponse response = service.getYearlyReport(2026);

        assertEquals(2026, response.year());
        assertEquals(12, response.months().size());
        assertEquals(new BigDecimal("1000.00"), response.months().get(0).incomeTotal());
        assertEquals(new BigDecimal("250.00"), response.months().get(0).ownerExpensesTotal());
        assertEquals(BigDecimal.ZERO, response.months().get(0).familyExpensesTotal());
        assertEquals(new BigDecimal("75.00"), response.months().get(0).savingsRate());
        assertEquals(BigDecimal.ZERO, response.months().get(1).incomeTotal());
        assertEquals(BigDecimal.ZERO, response.months().get(1).ownerExpensesTotal());
        assertEquals(BigDecimal.ZERO, response.months().get(1).familyExpensesTotal());
        assertNull(response.months().get(1).savingsRate());
        assertEquals(new BigDecimal("400.00"), response.months().get(2).familyExpensesTotal());
    }

    @Test
    void shouldCalculateYearlySavingsFromOwnerExpensesAndKeepFamilyExpensesSeparate() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2027, 1, 1);
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.sumIncomeByMonth(USER_ID, startDate, endDate)).thenReturn(List.of(new YearlyReportAmount(5, new BigDecimal("1000.00"))));
        when(repository.sumOwnerExpensesByMonth(USER_ID, startDate, endDate)).thenReturn(List.of(new YearlyReportAmount(5, new BigDecimal("200.00"))));
        when(repository.sumFamilyExpensesByMonth(USER_ID, startDate, endDate)).thenReturn(List.of(new YearlyReportAmount(5, new BigDecimal("300.00"))));

        YearlyReportResponse.MonthSummary may = service.getYearlyReport(2026).months().get(4);

        assertEquals(new BigDecimal("200.00"), may.ownerExpensesTotal());
        assertEquals(new BigDecimal("300.00"), may.familyExpensesTotal());
        assertEquals(new BigDecimal("80.00"), may.savingsRate());
        verify(repository).sumIncomeByMonth(USER_ID, startDate, endDate);
        verify(repository).sumOwnerExpensesByMonth(USER_ID, startDate, endDate);
        verify(repository).sumFamilyExpensesByMonth(USER_ID, startDate, endDate);
    }

    @Test
    void shouldRejectInvalidYearlyReportYear() {
        ResponseStatusException invalidYear = assertThrows(ResponseStatusException.class, () -> service.getYearlyReport(1899));

        assertEquals(HttpStatus.BAD_REQUEST, invalidYear.getStatusCode());
    }

    @Test
    void shouldBuildOpenInstallmentsReportOrderedByNextDueDate() {
        LocalDate today = LocalDate.now();
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.findOpenInstallmentItems(USER_ID, today)).thenReturn(List.of(
            new OpenInstallmentReportItem(
                SECOND_GROUP_ID,
                "Sofa",
                new BigDecimal("600.00"),
                6,
                SECOND_INSTALLMENT_ID,
                4,
                new BigDecimal("100.00"),
                today.plusDays(3),
                today.plusDays(3),
                new BigDecimal("300.00"),
                3
            ),
            new OpenInstallmentReportItem(
                GROUP_ID,
                "Notebook",
                new BigDecimal("1200.00"),
                12,
                INSTALLMENT_ID,
                5,
                new BigDecimal("100.00"),
                today.plusDays(10),
                today.plusDays(10),
                new BigDecimal("800.00"),
                8
            )
        ));

        OpenInstallmentsReportResponse response = service.getOpenInstallmentsReport();

        assertEquals(new BigDecimal("1100.00"), response.totalCommitted());
        assertEquals(2, response.groups().size());
        assertEquals(SECOND_GROUP_ID, response.groups().get(0).id());
        assertEquals("Sofa", response.groups().get(0).description());
        assertEquals(3, response.groups().get(0).paidInstallments());
        assertEquals(new BigDecimal("300.00"), response.groups().get(0).futureTotal());
        assertEquals(SECOND_INSTALLMENT_ID, response.groups().get(0).futureInstallments().get(0).id());
        assertEquals(GROUP_ID, response.groups().get(1).id());
    }

    @Test
    void shouldLoadOpenInstallmentsOnlyForAuthenticatedUser() {
        LocalDate today = LocalDate.now();
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.findOpenInstallmentItems(USER_ID, today)).thenReturn(List.of());

        OpenInstallmentsReportResponse response = service.getOpenInstallmentsReport();

        assertEquals(BigDecimal.ZERO, response.totalCommitted());
        assertEquals(List.of(), response.groups());
        verify(repository).findOpenInstallmentItems(USER_ID, today);
    }

    private User user() {
        User user = new User();
        user.setId(USER_ID);
        return user;
    }
}
