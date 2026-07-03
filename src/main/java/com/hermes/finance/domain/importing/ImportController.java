package com.hermes.finance.domain.importing;

import com.hermes.finance.dto.request.ImportExpenseRequest;
import com.hermes.finance.dto.request.ImportIncomeRequest;
import com.hermes.finance.dto.response.ImportBatchResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService service;

    public ImportController(ImportService service) {
        this.service = service;
    }

    @PostMapping("/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ImportBatchResponse importExpenses(@RequestBody List<ImportExpenseRequest> requests) {
        return service.importExpenses(requests);
    }

    @PostMapping("/incomes")
    @ResponseStatus(HttpStatus.CREATED)
    public ImportBatchResponse importIncomes(@RequestBody List<ImportIncomeRequest> requests) {
        return service.importIncomes(requests);
    }
}
