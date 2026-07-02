package com.hermes.finance.domain.expense;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.ExpenseCreateRequest;
import com.hermes.finance.dto.request.ExpenseInstallmentCreateRequest;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    private static final UUID USER_ID = UUID.fromString("0f35ce81-2f92-4547-baf5-9011cb879413");
    private static final UUID CATEGORY_ID = UUID.fromString("2c34ed5d-8d9f-4e7f-87e3-a34be90d60cb");
    private static final UUID MEMBER_ID = UUID.fromString("8bd15f7b-6cf3-4a95-8bc7-d244807e4620");
    private static final UUID GROUP_ID = UUID.fromString("6fcd1116-0357-4ba3-9617-ac65d32a054b");

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

    @Test
    void shouldCreateInstallmentsWithDatesAmountsAndGroup() {
        LocalDate firstDueDate = LocalDate.of(2026, 1, 15);
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.categoryIsAccessible(CATEGORY_ID, USER_ID)).thenReturn(true);
        when(repository.createInstallmentGroup(USER_ID, "Notebook", new BigDecimal("1200.00"), 12, firstDueDate))
            .thenReturn(GROUP_ID);
        when(repository.save(any(Expense.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        List<ExpenseResponse> responses = service.createInstallments(new ExpenseInstallmentCreateRequest(
            " Notebook ",
            new BigDecimal("1200.00"),
            12,
            firstDueDate,
            CATEGORY_ID,
            null,
            " credit "
        ));

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(repository, times(12)).save(expenseCaptor.capture());
        List<Expense> installments = expenseCaptor.getAllValues();
        assertEquals(12, responses.size());
        for (int index = 0; index < installments.size(); index++) {
            Expense installment = installments.get(index);
            assertEquals(USER_ID, installment.getUserId());
            assertEquals("Notebook", installment.getDescription());
            assertEquals(new BigDecimal("100.00"), installment.getAmount());
            assertEquals(firstDueDate.plusMonths(index), installment.getExpenseDate());
            assertEquals(GROUP_ID, installment.getInstallmentGroupId());
            assertEquals(index + 1, installment.getInstallmentNumber());
            assertEquals(12, installment.getTotalInstallments());
            assertEquals("owner", installment.getScope());
            assertEquals("credit", installment.getPaymentMethod());
        }
        verify(appLogger).info(eq(LoggingConstants.INSTALLMENT_GROUP_CREATED), eq(Map.of(
            "installmentGroupId", GROUP_ID,
            "totalInstallments", 12,
            "scope", "owner"
        )));
    }

    @Test
    void shouldDistributeInstallmentRemainderDeterministically() {
        LocalDate firstDueDate = LocalDate.of(2026, 2, 1);
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.createInstallmentGroup(USER_ID, "Compra", new BigDecimal("100.00"), 3, firstDueDate))
            .thenReturn(GROUP_ID);
        when(repository.save(any(Expense.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        service.createInstallments(validInstallmentRequest(null, null, new BigDecimal("100.00"), 3, firstDueDate));

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(repository, times(3)).save(expenseCaptor.capture());
        List<Expense> installments = expenseCaptor.getAllValues();
        assertEquals(new BigDecimal("33.34"), installments.get(0).getAmount());
        assertEquals(new BigDecimal("33.33"), installments.get(1).getAmount());
        assertEquals(new BigDecimal("33.33"), installments.get(2).getAmount());
    }

    @Test
    void shouldCreateFamilyInstallmentsWhenMemberBelongsToUser() {
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.familyMemberBelongsToUser(MEMBER_ID, USER_ID)).thenReturn(true);
        when(repository.createInstallmentGroup(eq(USER_ID), eq("Compra"), any(), eq(2), any())).thenReturn(GROUP_ID);
        when(repository.save(any(Expense.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        service.createInstallments(validInstallmentRequest(null, MEMBER_ID, new BigDecimal("200.00"), 2, LocalDate.of(2026, 3, 10)));

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(repository, times(2)).save(expenseCaptor.capture());
        for (Expense installment : expenseCaptor.getAllValues()) {
            assertEquals(MEMBER_ID, installment.getFamilyMemberId());
            assertEquals("family", installment.getScope());
        }
    }

    @Test
    void shouldRejectInstallmentForFamilyMemberFromAnotherUser() {
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.familyMemberBelongsToUser(MEMBER_ID, USER_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.createInstallments(
            validInstallmentRequest(null, MEMBER_ID, new BigDecimal("200.00"), 2, LocalDate.of(2026, 3, 10))
        ));

        verify(repository, never()).createInstallmentGroup(any(), any(), any(), eq(2), any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInstallmentWithInaccessibleCategory() {
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.categoryIsAccessible(CATEGORY_ID, USER_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.createInstallments(
            validInstallmentRequest(CATEGORY_ID, null, new BigDecimal("200.00"), 2, LocalDate.of(2026, 3, 10))
        ));

        verify(repository, never()).createInstallmentGroup(any(), any(), any(), eq(2), any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidInstallmentRequest() {
        assertThrows(ResponseStatusException.class, () -> service.createInstallments(validInstallmentRequest(null, null, BigDecimal.ZERO, 2, LocalDate.now())));
        assertThrows(ResponseStatusException.class, () -> service.createInstallments(validInstallmentRequest(null, null, BigDecimal.ONE, 1, LocalDate.now())));
        assertThrows(ResponseStatusException.class, () -> service.createInstallments(validInstallmentRequest(null, null, BigDecimal.ONE, 61, LocalDate.now())));
        assertThrows(ResponseStatusException.class, () -> service.createInstallments(validInstallmentRequest(null, null, BigDecimal.ONE, 2, null)));
    }

    @Test
    void shouldMarkInstallmentCreationAsTransactional() throws NoSuchMethodException {
        boolean transactional = ExpenseService.class
            .getMethod("createInstallments", ExpenseInstallmentCreateRequest.class)
            .isAnnotationPresent(Transactional.class);

        assertTrue(transactional);
    }

    @Test
    void shouldNotLogInstallmentFinancialData() {
        when(securityUtils.getCurrentUser()).thenReturn(user());
        when(repository.createInstallmentGroup(eq(USER_ID), eq("Compra"), any(), eq(2), any())).thenReturn(GROUP_ID);
        when(repository.save(any(Expense.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        service.createInstallments(validInstallmentRequest(null, null, new BigDecimal("200.00"), 2, LocalDate.of(2026, 3, 10)));

        ArgumentCaptor<Map<String, Object>> contextCaptor = ArgumentCaptor.forClass(Map.class);
        verify(appLogger).info(eq(LoggingConstants.INSTALLMENT_GROUP_CREATED), contextCaptor.capture());
        Map<String, Object> context = contextCaptor.getValue();
        assertFalse(context.containsKey("amount"));
        assertFalse(context.containsKey("totalAmount"));
        assertFalse(context.containsKey("description"));
    }

    private ExpenseCreateRequest validRequest(UUID categoryId, UUID memberId) {
        return validRequest(categoryId, memberId, new BigDecimal("50.00"), LocalDate.now(), "Mercado");
    }

    private ExpenseCreateRequest validRequest(UUID categoryId, UUID memberId, BigDecimal amount, LocalDate date, String description) {
        return new ExpenseCreateRequest(description, amount, date, categoryId, memberId, "card", "notes", false);
    }

    private ExpenseInstallmentCreateRequest validInstallmentRequest(UUID categoryId,
                                                                    UUID memberId,
                                                                    BigDecimal totalAmount,
                                                                    Integer totalInstallments,
                                                                    LocalDate firstDueDate) {
        return new ExpenseInstallmentCreateRequest("Compra", totalAmount, totalInstallments, firstDueDate, categoryId, memberId, "card");
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
