package com.hermes.finance.domain.family;

import com.hermes.finance.dto.request.FamilyMemberUpsertRequest;
import com.hermes.finance.dto.response.FamilyMemberResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/family-members")
public class FamilyMemberController {

    private final FamilyMemberService service;

    public FamilyMemberController(FamilyMemberService service) {
        this.service = service;
    }

    @GetMapping
    public List<FamilyMemberResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FamilyMemberResponse create(@Valid @RequestBody FamilyMemberUpsertRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public FamilyMemberResponse update(@PathVariable UUID id, @Valid @RequestBody FamilyMemberUpsertRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        service.deactivate(id);
    }
}
