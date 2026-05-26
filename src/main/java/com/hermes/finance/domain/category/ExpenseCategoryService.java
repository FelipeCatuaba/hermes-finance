package com.hermes.finance.domain.category;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.ExpenseCategoryUpsertRequest;
import com.hermes.finance.dto.response.ExpenseCategoryResponse;
import com.hermes.finance.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseCategoryService {

    private static final String DEFAULT_ICON = "shapes";
    private static final String DEFAULT_COLOR = "#64748B";

    private final ExpenseCategoryRepositoryPort repository;
    private final SecurityUtils securityUtils;

    public ExpenseCategoryService(ExpenseCategoryRepositoryPort repository, SecurityUtils securityUtils) {
        this.repository = repository;
        this.securityUtils = securityUtils;
    }

    public List<ExpenseCategoryResponse> list(boolean includeInactive) {
        User currentUser = securityUtils.getCurrentUser();
        return repository.findVisibleForUser(currentUser.getId(), includeInactive)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public ExpenseCategoryResponse create(ExpenseCategoryUpsertRequest request) {
        validateName(request.name());

        User currentUser = securityUtils.getCurrentUser();
        ExpenseCategory category = new ExpenseCategory();
        category.setUserId(currentUser.getId());
        category.setName(request.name().trim());
        category.setIcon(defaultIcon(request.icon()));
        category.setColorHex(defaultColor(request.colorHex()));
        category.setDefault(false);
        category.setActive(true);

        return toResponse(repository.save(category));
    }

    public ExpenseCategoryResponse update(UUID id, ExpenseCategoryUpsertRequest request) {
        validateName(request.name());
        ExpenseCategory category = requireEditableCategory(id);

        category.setName(request.name().trim());
        category.setIcon(defaultIcon(request.icon()));
        category.setColorHex(defaultColor(request.colorHex()));

        return toResponse(repository.update(category));
    }

    public void delete(UUID id) {
        ExpenseCategory category = requireEditableCategory(id);
        boolean inUse = repository.isCategoryInUse(id);
        if (inUse) {
            repository.deactivate(id);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Categoria em uso por gastos. Foi desativada e não pode ser removida.");
        }

        repository.deleteById(category.getId());
    }

    private ExpenseCategory requireEditableCategory(UUID id) {
        ExpenseCategory category = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));

        if (category.isDefault()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Categorias padrão do sistema não podem ser alteradas");
        }

        User currentUser = securityUtils.getCurrentUser();
        if (category.getUserId() == null || !category.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado à categoria informada");
        }

        return category;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }
        if (name.trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome deve ter no máximo 100 caracteres");
        }
    }

    private String defaultIcon(String icon) {
        if (icon == null || icon.trim().isEmpty()) {
            return DEFAULT_ICON;
        }
        return icon.trim();
    }

    private String defaultColor(String colorHex) {
        if (colorHex == null || colorHex.trim().isEmpty()) {
            return DEFAULT_COLOR;
        }
        return colorHex.trim();
    }

    private ExpenseCategoryResponse toResponse(ExpenseCategory category) {
        return new ExpenseCategoryResponse(
            category.getId(),
            category.getName(),
            category.getIcon(),
            category.getColorHex(),
            category.isDefault(),
            category.isActive(),
            category.getCreatedAt()
        );
    }
}
