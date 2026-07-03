package com.hermes.finance.domain.income;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.IncomeUpsertRequest;
import com.hermes.finance.dto.response.IncomeResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
class IncomeServiceTest {

    private static final UUID USER_ID = UUID.fromString("0f35ce81-2f92-4547-baf5-9011cb879413");
    private static final UUID OTHER_USER_ID = UUID.fromString("62212dbc-38db-4bfa-a986-d3a2c560420a");
    private static final UUID INCOME_ID = UUID.fromString("379ab046-4034-4f4a-a455-5868dde8f5bb");
    private static final UUID CATEGORY_ID = UUID.fromString("2c34ed5d-8d9f-4e7f-87e3-a34be90d60cb");

    @Mock
    private IncomeRepositoryPort repository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AppLogger appLogger;

    @InjectMocks
    private IncomeService service;

    @Test
    void shouldListCurrentUserIncomesForPeriod() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.findByUserAndPeriod(USER_ID, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1)))
            .thenReturn(List.of(persisted(validIncome(USER_ID))));

        List<IncomeResponse> response = service.list(3, 2026);

        assertEquals(1, response.size());
        assertEquals(INCOME_ID, response.get(0).id());
        verify(repository).findByUserAndPeriod(USER_ID, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1));
    }

    @Test
    void shouldCreateIncomeForCurrentUserWithoutFamilyMember() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.categoryIsAccessible(CATEGORY_ID, USER_ID)).thenReturn(true);
        when(repository.save(any(Income.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        IncomeResponse response = service.create(new IncomeUpsertRequest(
            " Salario ",
            new BigDecimal("8000.00"),
            LocalDate.now(),
            CATEGORY_ID,
            true,
            " pago no dia util "
        ));

        ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
        verify(repository).save(incomeCaptor.capture());
        Income saved = incomeCaptor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals("Salario", saved.getDescription());
        assertEquals("pago no dia util", saved.getNotes());
        assertEquals(CATEGORY_ID, saved.getCategoryId());
        assertEquals(true, saved.isRecurring());
        assertEquals("Salario", response.description());
        verify(appLogger).info(eq(LoggingConstants.INCOME_CREATED), eq(Map.of("incomeId", response.id())));
    }

    @Test
    void shouldUpdateOwnedIncome() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.findById(INCOME_ID)).thenReturn(Optional.of(persisted(validIncome(USER_ID))));
        when(repository.update(any(Income.class))).thenAnswer(invocation -> Optional.of(persisted(invocation.getArgument(0))));

        IncomeResponse response = service.update(INCOME_ID, validRequest(null));

        assertEquals(INCOME_ID, response.id());
        verify(repository).update(any(Income.class));
        verify(appLogger).info(eq(LoggingConstants.INCOME_UPDATED), eq(Map.of("incomeId", INCOME_ID)));
    }

    @Test
    void shouldDeleteOwnedIncome() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.findById(INCOME_ID)).thenReturn(Optional.of(persisted(validIncome(USER_ID))));

        service.delete(INCOME_ID);

        verify(repository).delete(INCOME_ID, USER_ID);
        verify(appLogger).info(eq(LoggingConstants.INCOME_DELETED), eq(Map.of("incomeId", INCOME_ID)));
    }

    @Test
    void shouldRejectIncomeFromAnotherUserWithForbidden() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.findById(INCOME_ID)).thenReturn(Optional.of(persisted(validIncome(OTHER_USER_ID))));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.update(INCOME_ID, validRequest(null)));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).update(any());
    }

    @Test
    void shouldRejectDeleteForIncomeFromAnotherUserWithForbidden() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.findById(INCOME_ID)).thenReturn(Optional.of(persisted(validIncome(OTHER_USER_ID))));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.delete(INCOME_ID));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).delete(any(), any());
    }

    @Test
    void shouldRejectInaccessibleCategory() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.categoryIsAccessible(CATEGORY_ID, USER_ID)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.create(validRequest(CATEGORY_ID)));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidAmountDateDescriptionAndPeriod() {
        assertThrows(ResponseStatusException.class, () -> service.create(validRequest(null, BigDecimal.ZERO, LocalDate.now(), "Salario")));
        assertThrows(ResponseStatusException.class, () -> service.create(validRequest(null, BigDecimal.ONE, LocalDate.now().plusDays(1), "Salario")));
        assertThrows(ResponseStatusException.class, () -> service.create(validRequest(null, BigDecimal.ONE, LocalDate.now(), " ")));
        assertThrows(ResponseStatusException.class, () -> service.list(13, 2026));
    }

    @Test
    void shouldNotLogFinancialData() {
        when(securityUtils.getCurrentUser()).thenReturn(user(USER_ID));
        when(repository.save(any(Income.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        service.create(validRequest(null));

        ArgumentCaptor<Map<String, Object>> contextCaptor = ArgumentCaptor.forClass(Map.class);
        verify(appLogger).info(eq(LoggingConstants.INCOME_CREATED), contextCaptor.capture());
        Map<String, Object> context = contextCaptor.getValue();
        assertFalse(context.containsKey("amount"));
        assertFalse(context.containsKey("description"));
        assertFalse(context.containsKey("notes"));
    }

    private IncomeUpsertRequest validRequest(UUID categoryId) {
        return validRequest(categoryId, new BigDecimal("8000.00"), LocalDate.now(), "Salario");
    }

    private IncomeUpsertRequest validRequest(UUID categoryId, BigDecimal amount, LocalDate date, String description) {
        return new IncomeUpsertRequest(description, amount, date, categoryId, false, "notes");
    }

    private Income validIncome(UUID userId) {
        Income income = new Income();
        income.setId(INCOME_ID);
        income.setUserId(userId);
        income.setDescription("Salario");
        income.setAmount(new BigDecimal("8000.00"));
        income.setIncomeDate(LocalDate.now());
        income.setRecurring(true);
        income.setNotes("notes");
        return income;
    }

    private Income persisted(Income income) {
        income.setId(INCOME_ID);
        income.setCreatedAt(OffsetDateTime.now());
        income.setUpdatedAt(OffsetDateTime.now());
        return income;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }
}
