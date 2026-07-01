package com.hermes.finance.domain.expense;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.ExpenseCreateRequest;
import com.hermes.finance.dto.response.ExpenseResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SecurityUtils;
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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    private static final UUID USER_ID = UUID.fromString("0f35ce81-2f92-4547-baf5-9011cb879413");
    private static final UUID CATEGORY_ID = UUID.fromString("2c34ed5d-8d9f-4e7f-87e3-a34be90d60cb");
    private static final UUID MEMBER_ID = UUID.fromString("8bd15f7b-6cf3-4a95-8bc7-d244807e4620");

    @Mock
    private ExpenseRepositoryPort repository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AppLogger appLogger;

    @InjectMocks
    private ExpenseService service;

    @Test
    void shouldCreateOwnerExpenseIgnoringClientOwnedFields() {
        User user = user();
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(repository.categoryIsAccessible(CATEGORY_ID, USER_ID)).thenReturn(true);
        when(repository.save(any(Expense.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        ExpenseResponse response = service.create(new ExpenseCreateRequest(
            " Mercado ",
            new BigDecimal("120.50"),
            LocalDate.now(),
            CATEGORY_ID,
            null,
            " pix ",
            " observacao ",
            true
        ));

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(repository).save(expenseCaptor.capture());
        Expense saved = expenseCaptor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals("Mercado", saved.getDescription());
        assertEquals("pix", saved.getPaymentMethod());
        assertEquals("observacao", saved.getNotes());
        assertEquals("owner", saved.getScope());
        assertEquals("owner", response.scope());
        verify(appLogger).info(eq(LoggingConstants.EXPENSE_CREATED), eq(Map.of(
            "expenseId", response.id(),
            "scope", "owner"
        )));
    }

    @Test
    void shouldCreateFamilyExpenseWhenMemberBelongsToUser() {
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.familyMemberBelongsToUser(MEMBER_ID, USER_ID)).thenReturn(true);
        when(repository.save(any(Expense.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        ExpenseResponse response = service.create(validRequest(null, MEMBER_ID));

        assertEquals("family", response.scope());
        assertEquals(MEMBER_ID, response.familyMemberId());
        verify(repository).familyMemberBelongsToUser(MEMBER_ID, USER_ID);
    }

    @Test
    void shouldRejectFamilyMemberFromAnotherUser() {
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.familyMemberBelongsToUser(MEMBER_ID, USER_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.create(validRequest(null, MEMBER_ID)));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInaccessibleCategory() {
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.categoryIsAccessible(CATEGORY_ID, USER_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.create(validRequest(CATEGORY_ID, null)));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidAmountDateAndDescription() {
        assertThrows(ResponseStatusException.class, () -> service.create(validRequest(null, null, BigDecimal.ZERO, LocalDate.now(), "Mercado")));
        assertThrows(ResponseStatusException.class, () -> service.create(validRequest(null, null, BigDecimal.ONE, LocalDate.now().plusDays(1), "Mercado")));
        assertThrows(ResponseStatusException.class, () -> service.create(validRequest(null, null, BigDecimal.ONE, LocalDate.now(), " ")));
    }

    @Test
    void shouldNotLogFinancialData() {
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.save(any(Expense.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        service.create(validRequest(null, null));

        ArgumentCaptor<Map<String, Object>> contextCaptor = ArgumentCaptor.forClass(Map.class);
        verify(appLogger).info(eq(LoggingConstants.EXPENSE_CREATED), contextCaptor.capture());
        Map<String, Object> context = contextCaptor.getValue();
        assertFalse(context.containsKey("amount"));
        assertFalse(context.containsKey("description"));
        assertFalse(context.containsKey("notes"));
    }

    private ExpenseCreateRequest validRequest(UUID categoryId, UUID memberId) {
        return validRequest(categoryId, memberId, new BigDecimal("50.00"), LocalDate.now(), "Mercado");
    }

    private ExpenseCreateRequest validRequest(UUID categoryId, UUID memberId, BigDecimal amount, LocalDate date, String description) {
        return new ExpenseCreateRequest(description, amount, date, categoryId, memberId, "card", "notes", false);
    }

    private User user() {
        User user = new User();
        user.setId(USER_ID);
        return user;
    }

    private Expense persisted(Expense expense) {
        expense.setId(UUID.fromString("379ab046-4034-4f4a-a455-5868dde8f5bb"));
        expense.setCreatedAt(OffsetDateTime.now());
        expense.setUpdatedAt(OffsetDateTime.now());
        return expense;
    }
}
