package com.hermes.finance.security;

import com.hermes.finance.config.TestJwtDecoderConfig;
import com.hermes.finance.domain.budget.BudgetService;
import com.hermes.finance.domain.expense.ExpenseService;
import com.hermes.finance.domain.income.IncomeService;
import com.hermes.finance.domain.report.MonthlyReportService;
import com.hermes.finance.dto.response.BudgetStatusResponse;
import com.hermes.finance.dto.response.ExpenseListResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestJwtDecoderConfig.class)
class ProtectedEndpointSecurityTest {

    private static final UUID RESOURCE_ID = UUID.fromString("379ab046-4034-4f4a-a455-5868dde8f5bb");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private IncomeService incomeService;

    @MockBean
    private BudgetService budgetService;

    @MockBean
    private MonthlyReportService monthlyReportService;

    @Test
    void shouldRejectProtectedEndpointsWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/expenses?month=3&year=2026"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/incomes?month=3&year=2026"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/budgets/status?month=3&year=2026"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/installment-groups/{id}", RESOURCE_ID))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(expenseService, incomeService, budgetService, monthlyReportService);
    }

    @Test
    void shouldAllowProtectedReadsWithMockedJwtAndDelegateToServices() throws Exception {
        when(expenseService.list(3, 2026, null, null, 0, 20))
            .thenReturn(new ExpenseListResponse(List.of(), BigDecimal.ZERO, 0, 0, 20, 0));
        when(incomeService.list(3, 2026)).thenReturn(List.of());
        when(budgetService.status(3, 2026)).thenReturn(new BudgetStatusResponse(3, 2026, List.of()));

        mockMvc.perform(get("/api/expenses?month=3&year=2026").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(0));
        mockMvc.perform(get("/api/incomes?month=3&year=2026").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
        mockMvc.perform(get("/api/budgets/status?month=3&year=2026").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.month").value(3));

        verify(expenseService).list(3, 2026, null, null, 0, 20);
        verify(incomeService).list(3, 2026);
        verify(budgetService).status(3, 2026);
    }

    @Test
    void shouldAllowProtectedMutationsWithMockedJwtAndDelegateToServices() throws Exception {
        String expensePayload = """
            {
              "description": "Mercado",
              "amount": 120.50,
              "expenseDate": "2026-03-10",
              "paymentMethod": "card",
              "notes": "safe"
            }
            """;
        String incomePayload = """
            {
              "description": "Salario",
              "amount": 8000.00,
              "incomeDate": "2026-03-05",
              "isRecurring": true
            }
            """;

        mockMvc.perform(post("/api/expenses")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(expensePayload))
            .andExpect(status().isCreated());
        mockMvc.perform(put("/api/incomes/{id}", RESOURCE_ID)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(incomePayload))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/installment-groups/{id}", RESOURCE_ID).with(jwt()))
            .andExpect(status().isNoContent());

        verify(expenseService).create(org.mockito.ArgumentMatchers.any());
        verify(incomeService).update(eq(RESOURCE_ID), org.mockito.ArgumentMatchers.any());
        verify(expenseService).deleteInstallmentGroup(RESOURCE_ID);
    }
}
