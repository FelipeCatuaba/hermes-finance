package com.hermes.finance.domain.budget;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.BudgetUpsertRequest;
import com.hermes.finance.dto.response.BudgetResponse;
import com.hermes.finance.dto.response.BudgetStatusResponse;
import com.hermes.finance.logging.AppLogger;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    private static final UUID USER_ID = UUID.fromString("0f35ce81-2f92-4547-baf5-9011cb879413");
    private static final UUID OTHER_USER_ID = UUID.fromString("62212dbc-38db-4bfa-a986-d3a2c560420a");
    private static final UUID BUDGET_ID = UUID.fromString("99b906ad-13d7-4d8a-a04e-6cf53ec1402a");
    private static final UUID CATEGORY_ID = UUID.fromString("2c34ed5d-8d9f-4e7f-87e3-a34be90d60cb");
    private static final UUID SECOND_CATEGORY_ID = UUID.fromString("f88a8fdb-4557-477f-b326-a80f5925ca1f");

    @Mock
    private BudgetRepositoryPort repository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AppLogger appLogger;

    @InjectMocks
    private BudgetService service;

    @Test
    void shouldBuildStatusWithNullPercentageForCategoryWithoutBudget() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.findStatus(USER_ID, 3, 2026, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1))).thenReturn(List.of(
            new BudgetStatusItem(CATEGORY_ID, "Mercado", "cart", "#22c55e", BUDGET_ID, new BigDecimal("500.00"), new BigDecimal("350.00")),
            new BudgetStatusItem(SECOND_CATEGORY_ID, "Lazer", "party-popper", "#f59e0b", null, null, new BigDecimal("100.00"))
        ));

        BudgetStatusResponse response = service.status(3, 2026);

        assertEquals(2, response.items().size());
        assertEquals(new BigDecimal("70.00"), response.items().get(0).pctUsed());
        assertFalse(response.items().get(0).overBudget());
        assertNull(response.items().get(1).amountLimit());
        assertNull(response.items().get(1).pctUsed());
        assertFalse(response.items().get(1).overBudget());
    }

    @Test
    void shouldMarkBudgetAsExceededWhenSpentReachesLimit() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.findStatus(USER_ID, 3, 2026, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1))).thenReturn(List.of(
            new BudgetStatusItem(CATEGORY_ID, "Mercado", "cart", "#22c55e", BUDGET_ID, new BigDecimal("500.00"), new BigDecimal("500.00"))
        ));

        BudgetStatusResponse.Item item = service.status(3, 2026).items().get(0);

        assertEquals(new BigDecimal("100.00"), item.pctUsed());
        assertTrue(item.overBudget());
    }

    @Test
    void shouldCreateBudgetForAccessibleCategory() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.categoryIsAccessible(CATEGORY_ID, USER_ID)).thenReturn(true);
        when(repository.upsert(any(Budget.class))).thenReturn(persistedBudget(USER_ID));

        BudgetResponse response = service.create(validRequest());

        assertEquals(BUDGET_ID, response.id());
        verify(repository).upsert(any(Budget.class));
    }

    @Test
    void shouldRejectInvalidPeriodAndLimit() {
        assertThrows(ResponseStatusException.class, () -> service.status(13, 2026));
        assertThrows(ResponseStatusException.class, () -> service.status(3, 1899));
        assertThrows(ResponseStatusException.class, () -> service.create(new BudgetUpsertRequest(CATEGORY_ID, 3, 2026, BigDecimal.ZERO)));
    }

    @Test
    void shouldRejectInaccessibleCategory() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.categoryIsAccessible(CATEGORY_ID, USER_ID)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.create(validRequest()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).upsert(any());
    }

    @Test
    void shouldRejectUpdateForBudgetFromAnotherUser() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.findById(BUDGET_ID)).thenReturn(Optional.of(persistedBudget(OTHER_USER_ID)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.update(BUDGET_ID, validRequest()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).update(any());
    }

    private BudgetUpsertRequest validRequest() {
        return new BudgetUpsertRequest(CATEGORY_ID, 3, 2026, new BigDecimal("500.00"));
    }

    private Budget persistedBudget(UUID userId) {
        Budget budget = new Budget();
        budget.setId(BUDGET_ID);
        budget.setUserId(userId);
        budget.setCategoryId(CATEGORY_ID);
        budget.setMonth(3);
        budget.setYear(2026);
        budget.setAmountLimit(new BigDecimal("500.00"));
        budget.setCreatedAt(OffsetDateTime.now());
        return budget;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }
}
