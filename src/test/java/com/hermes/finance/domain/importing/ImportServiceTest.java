package com.hermes.finance.domain.importing;

import com.hermes.finance.domain.expense.ExpenseService;
import com.hermes.finance.domain.income.IncomeService;
import com.hermes.finance.dto.request.ExpenseCreateRequest;
import com.hermes.finance.dto.request.ExpenseInstallmentCreateRequest;
import com.hermes.finance.dto.request.ImportExpenseRequest;
import com.hermes.finance.dto.request.ImportIncomeRequest;
import com.hermes.finance.dto.request.IncomeUpsertRequest;
import com.hermes.finance.dto.response.ExpenseResponse;
import com.hermes.finance.dto.response.ImportBatchResponse;
import com.hermes.finance.dto.response.IncomeResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    private static final UUID CATEGORY_ID = UUID.fromString("2c34ed5d-8d9f-4e7f-87e3-a34be90d60cb");
    private static final UUID MEMBER_ID = UUID.fromString("8bd15f7b-6cf3-4a95-8bc7-d244807e4620");
    private static final UUID EXPENSE_ID = UUID.fromString("379ab046-4034-4f4a-a455-5868dde8f5bb");
    private static final UUID INCOME_ID = UUID.fromString("ac405b11-1f7d-403d-ab17-51d3b206630a");

    @Mock
    private ExpenseService expenseService;

    @Mock
    private IncomeService incomeService;

    @Mock
    private AppLogger appLogger;

    @InjectMocks
    private ImportService service;

    @Test
    void shouldImportExpensesAndContinueAfterInvalidRows() {
        when(expenseService.create(any())).thenReturn(expense(LocalDate.of(2026, 3, 10)));
        when(expenseService.createInstallments(any())).thenReturn(List.of(
            expense(LocalDate.of(2026, 4, 5)),
            expense(LocalDate.of(2026, 5, 5))
        ));

        ImportBatchResponse response = service.importExpenses(Arrays.asList(
            expenseRequest(7, 1),
            expenseRequest(8, 2),
            null
        ));

        assertEquals(3, response.imported());
        assertEquals(1, response.failed());
        assertEquals(List.of("2026-03", "2026-04", "2026-05"), response.monthsAffected());
        assertEquals(2, response.errors().get(0).index());
        assertEquals("Gasto e obrigatorio", response.errors().get(0).message());

        ArgumentCaptor<ExpenseInstallmentCreateRequest> installmentCaptor = ArgumentCaptor.forClass(ExpenseInstallmentCreateRequest.class);
        verify(expenseService).createInstallments(installmentCaptor.capture());
        assertEquals(new BigDecimal("300.00"), installmentCaptor.getValue().totalAmount());
        assertEquals(2, installmentCaptor.getValue().totalInstallments());
        assertEquals(LocalDate.of(2026, 4, 5), installmentCaptor.getValue().firstDueDate());

        verify(appLogger).info(eq(LoggingConstants.IMPORT_COMPLETED), eq(Map.of(
            "type", "expenses",
            "importedCount", 3,
            "failedCount", 1,
            "monthsAffectedCount", 3
        )));
    }

    @Test
    void shouldImportIncomesWithLineLevelErrors() {
        when(incomeService.create(any())).thenReturn(income(LocalDate.of(2026, 2, 15)));

        ImportBatchResponse response = service.importIncomes(Arrays.asList(
            incomeRequest(3),
            null
        ));

        assertEquals(1, response.imported());
        assertEquals(1, response.failed());
        assertEquals(List.of("2026-02"), response.monthsAffected());
        assertEquals(1, response.errors().get(0).index());

        ArgumentCaptor<IncomeUpsertRequest> incomeCaptor = ArgumentCaptor.forClass(IncomeUpsertRequest.class);
        verify(incomeService).create(incomeCaptor.capture());
        assertEquals("Salario", incomeCaptor.getValue().description());
        assertEquals(CATEGORY_ID, incomeCaptor.getValue().categoryId());

        verify(appLogger).info(eq(LoggingConstants.IMPORT_COMPLETED), eq(Map.of(
            "type", "incomes",
            "importedCount", 1,
            "failedCount", 1,
            "monthsAffectedCount", 1
        )));
    }

    @Test
    void shouldRejectEmptyBatches() {
        assertThrows(ResponseStatusException.class, () -> service.importExpenses(List.of()));
        assertThrows(ResponseStatusException.class, () -> service.importIncomes(List.of()));
    }

    @Test
    void shouldNotLogFinancialData() {
        when(expenseService.create(any())).thenReturn(expense(LocalDate.of(2026, 3, 10)));

        service.importExpenses(List.of(expenseRequest(0, 1)));

        ArgumentCaptor<Map<String, Object>> contextCaptor = ArgumentCaptor.forClass(Map.class);
        verify(appLogger).info(eq(LoggingConstants.IMPORT_COMPLETED), contextCaptor.capture());
        Map<String, Object> context = contextCaptor.getValue();
        assertFalse(context.containsKey("amount"));
        assertFalse(context.containsKey("description"));
        assertFalse(context.containsKey("notes"));
    }

    private ImportExpenseRequest expenseRequest(int index, int totalInstallments) {
        return new ImportExpenseRequest(
            index,
            "Mercado",
            new BigDecimal(totalInstallments > 1 ? "300.00" : "120.50"),
            LocalDate.of(2026, 3, 10),
            CATEGORY_ID,
            MEMBER_ID,
            "card",
            "notes",
            false,
            totalInstallments,
            LocalDate.of(2026, 4, 5)
        );
    }

    private ImportIncomeRequest incomeRequest(int index) {
        return new ImportIncomeRequest(
            index,
            "Salario",
            new BigDecimal("4500.00"),
            LocalDate.of(2026, 2, 15),
            CATEGORY_ID,
            true,
            "CLT"
        );
    }

    private ExpenseResponse expense(LocalDate date) {
        return new ExpenseResponse(
            EXPENSE_ID,
            "Mercado",
            new BigDecimal("120.50"),
            date,
            CATEGORY_ID,
            MEMBER_ID,
            null,
            null,
            null,
            "card",
            "notes",
            false,
            "family",
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }

    private IncomeResponse income(LocalDate date) {
        return new IncomeResponse(
            INCOME_ID,
            "Salario",
            new BigDecimal("4500.00"),
            date,
            CATEGORY_ID,
            true,
            "CLT",
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }
}
