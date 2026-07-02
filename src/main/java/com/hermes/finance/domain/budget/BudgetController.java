package com.hermes.finance.domain.budget;

import com.hermes.finance.dto.request.BudgetUpsertRequest;
import com.hermes.finance.dto.response.BudgetResponse;
import com.hermes.finance.dto.response.BudgetStatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    @GetMapping
    public List<BudgetResponse> list(@RequestParam int month, @RequestParam int year) {
        return service.list(month, year);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@Valid @RequestBody BudgetUpsertRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public BudgetResponse update(@PathVariable UUID id, @Valid @RequestBody BudgetUpsertRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping("/status")
    public BudgetStatusResponse status(@RequestParam int month, @RequestParam int year) {
        return service.status(month, year);
    }

    @PostMapping("/copy-previous")
    public List<BudgetResponse> copyPrevious(@RequestParam int month, @RequestParam int year) {
        return service.copyPreviousMonth(month, year);
    }
}
