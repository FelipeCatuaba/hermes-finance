package com.hermes.finance.domain.expense;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.ExpenseCreateRequest;
import com.hermes.finance.dto.request.ExpenseInstallmentCreateRequest;
import com.hermes.finance.dto.response.ExpenseResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Transactional
    public List<ExpenseResponse> createInstallments(ExpenseInstallmentCreateRequest request) {
        validateInstallment(request);

        User currentUser = securityUtils.getCurrentUser();
        if (request.familyMemberId() != null
            && !repository.familyMemberBelongsToUser(request.familyMemberId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Membro nao pertence ao usuario");
        }

        if (request.categoryId() != null && !repository.categoryIsAccessible(request.categoryId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Categoria nao acessivel ao usuario");
        }

        String description = request.description().trim();
        UUID groupId = repository.createInstallmentGroup(
            currentUser.getId(),
            description,
            request.totalAmount(),
            request.totalInstallments(),
            request.firstDueDate()
        );

        List<BigDecimal> amounts = splitAmount(request.totalAmount(), request.totalInstallments());
        List<ExpenseResponse> responses = new ArrayList<>();
        String scope = request.familyMemberId() == null ? "owner" : "family";
        String paymentMethod = trimToNull(request.paymentMethod());

        for (int index = 0; index < request.totalInstallments(); index++) {
            Expense expense = new Expense();
            expense.setUserId(currentUser.getId());
            expense.setDescription(description);
            expense.setAmount(amounts.get(index));
            expense.setExpenseDate(request.firstDueDate().plusMonths(index));
            expense.setCategoryId(request.categoryId());
            expense.setFamilyMemberId(request.familyMemberId());
            expense.setInstallmentGroupId(groupId);
            expense.setInstallmentNumber(index + 1);
            expense.setTotalInstallments(request.totalInstallments());
            expense.setPaymentMethod(paymentMethod);
            expense.setScope(scope);

            responses.add(toResponse(repository.save(expense)));
        }

        appLogger.info(LoggingConstants.INSTALLMENT_GROUP_CREATED, Map.of(
            "installmentGroupId", groupId,
            "totalInstallments", request.totalInstallments(),
            "scope", scope
        ));

        return responses;
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

    private void validateInstallment(ExpenseInstallmentCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parcelamento e obrigatorio");
        }
        if (request.description() == null || request.description().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Descricao e obrigatoria");
        }
        if (request.description().trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Descricao deve ter no maximo 255 caracteres");
        }
        if (request.totalAmount() == null || request.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor total deve ser maior que zero");
        }
        if (request.totalAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor excede o limite permitido");
        }
        if (request.totalInstallments() == null || request.totalInstallments() < 2 || request.totalInstallments() > 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Numero de parcelas deve estar entre 2 e 60");
        }
        if (request.firstDueDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Primeira data de vencimento e obrigatoria");
        }
    }

    private List<BigDecimal> splitAmount(BigDecimal totalAmount, int totalInstallments) {
        long totalCents = totalAmount.setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValueExact();
        long baseCents = totalCents / totalInstallments;
        long remainder = totalCents % totalInstallments;

        List<BigDecimal> amounts = new ArrayList<>();
        for (int index = 0; index < totalInstallments; index++) {
            long cents = baseCents + (index < remainder ? 1 : 0);
            amounts.add(BigDecimal.valueOf(cents, 2));
        }
        return amounts;
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
            expense.getInstallmentGroupId(),
            expense.getInstallmentNumber(),
            expense.getTotalInstallments(),
            expense.getPaymentMethod(),
            expense.getNotes(),
            expense.isFixed(),
            expense.getScope(),
            expense.getCreatedAt(),
            expense.getUpdatedAt()
        );
    }
}
