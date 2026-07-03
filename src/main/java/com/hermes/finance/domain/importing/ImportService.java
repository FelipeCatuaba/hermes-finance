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
import com.hermes.finance.dto.response.ImportItemError;
import com.hermes.finance.dto.response.IncomeResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class ImportService {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final AppLogger appLogger;

    public ImportService(ExpenseService expenseService, IncomeService incomeService, AppLogger appLogger) {
        this.expenseService = expenseService;
        this.incomeService = incomeService;
        this.appLogger = appLogger;
    }

    public ImportBatchResponse importExpenses(List<ImportExpenseRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Lista de gastos e obrigatoria");
        }

        List<ImportItemError> errors = new ArrayList<>();
        TreeSet<YearMonth> monthsAffected = new TreeSet<>();
        int imported = 0;
        int failed = 0;

        for (int position = 0; position < requests.size(); position++) {
            ImportExpenseRequest request = requests.get(position);
            int index = rowIndex(request == null ? null : request.index(), position);
            try {
                if (request == null) {
                    throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Gasto e obrigatorio");
                }
                if (isInstallment(request)) {
                    List<ExpenseResponse> created = expenseService.createInstallments(toInstallmentRequest(request));
                    created.forEach(expense -> monthsAffected.add(YearMonth.from(expense.expenseDate())));
                    imported += created.size();
                } else {
                    ExpenseResponse created = expenseService.create(toExpenseRequest(request));
                    monthsAffected.add(YearMonth.from(created.expenseDate()));
                    imported++;
                }
            } catch (ResponseStatusException exception) {
                failed++;
                errors.add(new ImportItemError(index, "item", exception.getReason() == null ? "Registro invalido" : exception.getReason()));
            }
        }

        logCompleted("expenses", imported, failed, monthsAffected.size());
        return new ImportBatchResponse(imported, failed, errors, toMonthStrings(monthsAffected));
    }

    public ImportBatchResponse importIncomes(List<ImportIncomeRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Lista de receitas e obrigatoria");
        }

        List<ImportItemError> errors = new ArrayList<>();
        TreeSet<YearMonth> monthsAffected = new TreeSet<>();
        int imported = 0;
        int failed = 0;

        for (int position = 0; position < requests.size(); position++) {
            ImportIncomeRequest request = requests.get(position);
            int index = rowIndex(request == null ? null : request.index(), position);
            try {
                if (request == null) {
                    throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Receita e obrigatoria");
                }
                IncomeResponse created = incomeService.create(toIncomeRequest(request));
                monthsAffected.add(YearMonth.from(created.incomeDate()));
                imported++;
            } catch (ResponseStatusException exception) {
                failed++;
                errors.add(new ImportItemError(index, "item", exception.getReason() == null ? "Registro invalido" : exception.getReason()));
            }
        }

        logCompleted("incomes", imported, failed, monthsAffected.size());
        return new ImportBatchResponse(imported, failed, errors, toMonthStrings(monthsAffected));
    }

    private boolean isInstallment(ImportExpenseRequest request) {
        return request != null && request.totalInstallments() != null && request.totalInstallments() > 1;
    }

    private ExpenseCreateRequest toExpenseRequest(ImportExpenseRequest request) {
        return new ExpenseCreateRequest(
            request == null ? null : request.description(),
            request == null ? null : request.amount(),
            request == null ? null : request.expenseDate(),
            request == null ? null : request.categoryId(),
            request == null ? null : request.familyMemberId(),
            request == null ? null : request.paymentMethod(),
            request == null ? null : request.notes(),
            request == null ? null : request.isFixed()
        );
    }

    private ExpenseInstallmentCreateRequest toInstallmentRequest(ImportExpenseRequest request) {
        LocalDate firstDueDate = request.firstDueDate() == null ? request.expenseDate() : request.firstDueDate();
        return new ExpenseInstallmentCreateRequest(
            request.description(),
            request.amount(),
            request.totalInstallments(),
            firstDueDate,
            request.categoryId(),
            request.familyMemberId(),
            request.paymentMethod()
        );
    }

    private IncomeUpsertRequest toIncomeRequest(ImportIncomeRequest request) {
        return new IncomeUpsertRequest(
            request == null ? null : request.description(),
            request == null ? null : request.amount(),
            request == null ? null : request.incomeDate(),
            request == null ? null : request.categoryId(),
            request == null ? null : request.isRecurring(),
            request == null ? null : request.notes()
        );
    }

    private int rowIndex(Integer requestIndex, int position) {
        return requestIndex == null ? position : requestIndex;
    }

    private List<String> toMonthStrings(TreeSet<YearMonth> monthsAffected) {
        return monthsAffected.stream()
            .map(YearMonth::toString)
            .toList();
    }

    private void logCompleted(String type, int imported, int failed, int monthsAffectedCount) {
        appLogger.info(LoggingConstants.IMPORT_COMPLETED, Map.of(
            "type", type,
            "importedCount", imported,
            "failedCount", failed,
            "monthsAffectedCount", monthsAffectedCount
        ));
    }
}
