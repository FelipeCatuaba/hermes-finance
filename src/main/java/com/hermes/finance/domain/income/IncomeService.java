package com.hermes.finance.domain.income;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.IncomeUpsertRequest;
import com.hermes.finance.dto.response.IncomeResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IncomeService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");

    private final IncomeRepositoryPort repository;
    private final SecurityUtils securityUtils;
    private final AppLogger appLogger;

    public IncomeService(IncomeRepositoryPort repository, SecurityUtils securityUtils, AppLogger appLogger) {
        this.repository = repository;
        this.securityUtils = securityUtils;
        this.appLogger = appLogger;
    }

    public List<IncomeResponse> list(int month, int year) {
        YearMonth period = validatePeriod(month, year);
        User currentUser = securityUtils.getCurrentUser();
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.plusMonths(1).atDay(1);

        return repository.findByUserAndPeriod(currentUser.getId(), startDate, endDate)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public IncomeResponse create(IncomeUpsertRequest request) {
        validate(request);
        User currentUser = securityUtils.getCurrentUser();
        validateCategoryAccess(request.categoryId(), currentUser.getId());

        Income income = new Income();
        income.setUserId(currentUser.getId());
        applyRequest(income, request);

        Income saved = repository.save(income);
        appLogger.info(LoggingConstants.INCOME_CREATED, Map.of("incomeId", saved.getId()));
        return toResponse(saved);
    }

    public IncomeResponse update(UUID id, IncomeUpsertRequest request) {
        validate(request);
        Income existing = requireOwnedIncome(id);
        validateCategoryAccess(request.categoryId(), existing.getUserId());

        applyRequest(existing, request);
        Income updated = repository.update(existing)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receita nao encontrada"));

        appLogger.info(LoggingConstants.INCOME_UPDATED, Map.of("incomeId", updated.getId()));
        return toResponse(updated);
    }

    public void delete(UUID id) {
        Income existing = requireOwnedIncome(id);
        repository.delete(existing.getId(), existing.getUserId());
        appLogger.info(LoggingConstants.INCOME_DELETED, Map.of("incomeId", existing.getId()));
    }

    private Income requireOwnedIncome(UUID id) {
        Income income = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receita nao encontrada"));

        User currentUser = securityUtils.getCurrentUser();
        if (!income.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a receita informada");
        }

        return income;
    }

    private void validate(IncomeUpsertRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receita e obrigatoria");
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
        if (request.incomeDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data da receita e obrigatoria");
        }
        if (request.incomeDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data futura nao permitida");
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
        if (categoryId != null && !repository.categoryIsAccessible(categoryId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Categoria nao acessivel ao usuario");
        }
    }

    private void applyRequest(Income income, IncomeUpsertRequest request) {
        income.setDescription(request.description().trim());
        income.setAmount(request.amount());
        income.setIncomeDate(request.incomeDate());
        income.setCategoryId(request.categoryId());
        income.setRecurring(Boolean.TRUE.equals(request.isRecurring()));
        income.setNotes(trimToNull(request.notes()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private IncomeResponse toResponse(Income income) {
        return new IncomeResponse(
            income.getId(),
            income.getDescription(),
            income.getAmount(),
            income.getIncomeDate(),
            income.getCategoryId(),
            income.isRecurring(),
            income.getNotes(),
            income.getCreatedAt(),
            income.getUpdatedAt()
        );
    }
}
