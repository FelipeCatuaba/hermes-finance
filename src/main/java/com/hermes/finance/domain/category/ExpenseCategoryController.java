package com.hermes.finance.domain.category;

import com.hermes.finance.dto.request.ExpenseCategoryUpsertRequest;
import com.hermes.finance.dto.response.ExpenseCategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService service;

    public ExpenseCategoryController(ExpenseCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExpenseCategoryResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseCategoryResponse create(@Valid @RequestBody ExpenseCategoryUpsertRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ExpenseCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody ExpenseCategoryUpsertRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
