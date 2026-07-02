package com.hermes.finance.domain.budget;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.BudgetUpsertRequest;
import com.hermes.finance.dto.response.BudgetResponse;
import com.hermes.finance.dto.response.BudgetStatusResponse;
import com.hermes.finance.dto.response.ExpenseCategorySummaryResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BudgetService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");

    private final BudgetRepositoryPort repository;
    private final SecurityUtils securityUtils;
    private final AppLogger appLogger;

    public BudgetService(BudgetRepositoryPort repository, SecurityUtils securityUtils, AppLogger appLogger) {
        this.repository = repository;
        this.securityUtils = securityUtils;
        this.appLogger = appLogger;
    }

    public List<BudgetResponse> list(int month, int year) {
        validatePeriod(month, year);
        User currentUser = securityUtils.getCurrentUser();
        return repository.findByUserAndPeriod(currentUser.getId(), month, year)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public BudgetResponse create(BudgetUpsertRequest request) {
        validate(request);
        User currentUser = securityUtils.getCurrentUser();
        validateCategoryAccess(request.categoryId(), currentUser.getId());

        Budget budget = toBudget(request, currentUser.getId());
        Budget saved = repository.upsert(budget);
        appLogger.info(LoggingConstants.BUDGET_UPDATED, Map.of(
            "budgetId", saved.getId(),
            "categoryId", saved.getCategoryId(),
            "month", saved.getMonth(),
            "year", saved.getYear()
        ));
        return toResponse(saved);
    }

    public BudgetResponse update(UUID id, BudgetUpsertRequest request) {
        validate(request);
        Budget existing = requireOwnedBudget(id);
        validateCategoryAccess(request.categoryId(), existing.getUserId());

        existing.setCategoryId(request.categoryId());
        existing.setMonth(request.month());
        existing.setYear(request.year());
        existing.setAmountLimit(request.amountLimit());

        Budget updated = repository.update(existing)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orcamento nao encontrado"));
        appLogger.info(LoggingConstants.BUDGET_UPDATED, Map.of("budgetId", updated.getId()));
        return toResponse(updated);
    }

    public void delete(UUID id) {
        Budget existing = requireOwnedBudget(id);
        repository.delete(existing.getId(), existing.getUserId());
        appLogger.info(LoggingConstants.BUDGET_DELETED, Map.of("budgetId", existing.getId()));
    }

    public BudgetStatusResponse status(int month, int year) {
        YearMonth period = validatePeriod(month, year);
        User currentUser = securityUtils.getCurrentUser();
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.plusMonths(1).atDay(1);

        List<BudgetStatusResponse.Item> items = repository.findStatus(currentUser.getId(), month, year, startDate, endDate)
            .stream()
            .map(this::toStatusItem)
            .toList();
        return new BudgetStatusResponse(month, year, items);
    }

    public List<BudgetResponse> copyPreviousMonth(int month, int year) {
        YearMonth target = validatePeriod(month, year);
        YearMonth previous = target.minusMonths(1);
        User currentUser = securityUtils.getCurrentUser();

        repository.copyPreviousMonth(currentUser.getId(), previous.getMonthValue(), previous.getYear(), month, year);
        appLogger.info(LoggingConstants.BUDGET_UPDATED, Map.of("month", month, "year", year, "sourceMonth", previous.getMonthValue(), "sourceYear", previous.getYear()));
        return repository.findByUserAndPeriod(currentUser.getId(), month, year)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private Budget requireOwnedBudget(UUID id) {
        Budget budget = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orcamento nao encontrado"));
        User currentUser = securityUtils.getCurrentUser();
        if (!budget.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado ao orcamento informado");
        }
        return budget;
    }

    private void validate(BudgetUpsertRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orcamento e obrigatorio");
        }
        validatePeriod(request.month(), request.year());
        if (request.categoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria e obrigatoria");
        }
        if (request.amountLimit() == null || request.amountLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limite deve ser maior que zero");
        }
        if (request.amountLimit().compareTo(MAX_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limite excede o valor permitido");
        }
    }

    private YearMonth validatePeriod(int month, int year) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mes invalido");
        }
        if (year < 1900 || year > 9999) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ano invalido");
        }
        return YearMonth.of(year, month);
    }

    private void validateCategoryAccess(UUID categoryId, UUID userId) {
        if (!repository.categoryIsAccessible(categoryId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Categoria nao acessivel ao usuario");
        }
    }

    private Budget toBudget(BudgetUpsertRequest request, UUID userId) {
        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(request.categoryId());
        budget.setMonth(request.month());
        budget.setYear(request.year());
        budget.setAmountLimit(request.amountLimit());
        return budget;
    }

    private BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(
            budget.getId(),
            budget.getCategoryId(),
            budget.getMonth(),
            budget.getYear(),
            budget.getAmountLimit(),
            budget.getCreatedAt()
        );
    }

    private BudgetStatusResponse.Item toStatusItem(BudgetStatusItem item) {
        BigDecimal pctUsed = percentage(item.spentAmount(), item.amountLimit());
        return new BudgetStatusResponse.Item(
            new ExpenseCategorySummaryResponse(item.categoryId(), item.categoryName(), item.categoryIcon(), item.categoryColorHex()),
            item.budgetId(),
            item.amountLimit(),
            item.spentAmount(),
            pctUsed,
            item.amountLimit() != null && item.spentAmount().compareTo(item.amountLimit()) >= 0
        );
    }

    private BigDecimal percentage(BigDecimal amount, BigDecimal base) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return amount.multiply(new BigDecimal("100")).divide(base, 2, RoundingMode.HALF_UP);
    }
}
