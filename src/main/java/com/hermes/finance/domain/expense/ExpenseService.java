package com.hermes.finance.domain.expense;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.ExpenseCreateRequest;
import com.hermes.finance.dto.response.ExpenseResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class ExpenseService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");

    private final ExpenseRepositoryPort repository;
    private final SecurityUtils securityUtils;
    private final AppLogger appLogger;

    public ExpenseService(ExpenseRepositoryPort repository, SecurityUtils securityUtils, AppLogger appLogger) {
        this.repository = repository;
        this.securityUtils = securityUtils;
        this.appLogger = appLogger;
    }

    public ExpenseResponse create(ExpenseCreateRequest request) {
        validate(request);

        User currentUser = securityUtils.getCurrentUser();
        if (request.familyMemberId() != null
            && !repository.familyMemberBelongsToUser(request.familyMemberId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Membro nao pertence ao usuario");
        }

        if (request.categoryId() != null && !repository.categoryIsAccessible(request.categoryId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Categoria nao acessivel ao usuario");
        }

        Expense expense = new Expense();
        expense.setUserId(currentUser.getId());
        expense.setDescription(request.description().trim());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setCategoryId(request.categoryId());
        expense.setFamilyMemberId(request.familyMemberId());
        expense.setPaymentMethod(trimToNull(request.paymentMethod()));
        expense.setNotes(trimToNull(request.notes()));
        expense.setFixed(Boolean.TRUE.equals(request.isFixed()));
        expense.setScope(request.familyMemberId() == null ? "owner" : "family");

        Expense saved = repository.save(expense);
        appLogger.info(LoggingConstants.EXPENSE_CREATED, Map.of(
            "expenseId", saved.getId(),
            "scope", saved.getScope()
        ));

        return toResponse(saved);
    }

    private void validate(ExpenseCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gasto e obrigatorio");
        }
        if (request.description() == null || request.description().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Descricao e obrigatoria");
        }
        if (request.description().trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Descricao deve ter no maximo 255 caracteres");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor deve ser maior que zero");
        }
        if (request.amount().compareTo(MAX_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor excede o limite permitido");
        }
        if (request.expenseDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data do gasto e obrigatoria");
        }
        if (request.expenseDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data futura nao permitida");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            expense.getDescription(),
            expense.getAmount(),
            expense.getExpenseDate(),
            expense.getCategoryId(),
            expense.getFamilyMemberId(),
            expense.getPaymentMethod(),
            expense.getNotes(),
            expense.isFixed(),
            expense.getScope(),
            expense.getCreatedAt(),
            expense.getUpdatedAt()
        );
    }
}
