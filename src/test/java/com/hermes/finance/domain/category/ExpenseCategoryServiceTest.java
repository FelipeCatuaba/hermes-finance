package com.hermes.finance.domain.category;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.ExpenseCategoryUpsertRequest;
import com.hermes.finance.dto.response.ExpenseCategoryResponse;
import com.hermes.finance.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryServiceTest {

    @Mock
    private ExpenseCategoryRepositoryPort repository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ExpenseCategoryService service;

    @Test
    void shouldCreateCategoryWithDefaults() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(securityUtils.getCurrentUser()).thenReturn(user);

        ExpenseCategory saved = new ExpenseCategory();
        saved.setId(UUID.randomUUID());
        saved.setUserId(user.getId());
        saved.setName("Pets");
        saved.setIcon("shapes");
        saved.setColorHex("#64748B");
        saved.setActive(true);
        when(repository.save(any(ExpenseCategory.class))).thenReturn(saved);

        ExpenseCategoryResponse response = service.create(new ExpenseCategoryUpsertRequest(" Pets ", " ", " "));

        assertThat(response.name()).isEqualTo("Pets");
        assertThat(response.icon()).isEqualTo("shapes");
        assertThat(response.colorHex()).isEqualTo("#64748B");
    }

    @Test
    void shouldListVisibleCategories() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(securityUtils.getCurrentUser()).thenReturn(user);

        ExpenseCategory category = new ExpenseCategory();
        category.setId(UUID.randomUUID());
        category.setUserId(user.getId());
        category.setName("Moradia");
        category.setDefault(true);
        category.setActive(true);
        when(repository.findVisibleForUser(user.getId(), false)).thenReturn(List.of(category));

        List<ExpenseCategoryResponse> list = service.list(false);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).isDefault()).isTrue();
    }

    @Test
    void shouldForbidDeleteForDefaultCategory() {
        UUID id = UUID.randomUUID();
        ExpenseCategory category = new ExpenseCategory();
        category.setId(id);
        category.setDefault(true);
        when(repository.findById(id)).thenReturn(Optional.of(category));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.delete(id));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldConflictAndDeactivateWhenCategoryInUse() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        when(securityUtils.getCurrentUser()).thenReturn(user);

        ExpenseCategory category = new ExpenseCategory();
        category.setId(id);
        category.setUserId(user.getId());
        category.setDefault(false);
        when(repository.findById(id)).thenReturn(Optional.of(category));
        when(repository.isCategoryInUse(id)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.delete(id));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(repository).deactivate(id);
    }

    @Test
    void shouldDeleteCategoryWhenNotInUse() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        when(securityUtils.getCurrentUser()).thenReturn(user);

        ExpenseCategory category = new ExpenseCategory();
        category.setId(id);
        category.setUserId(user.getId());
        category.setDefault(false);
        when(repository.findById(id)).thenReturn(Optional.of(category));
        when(repository.isCategoryInUse(id)).thenReturn(false);

        service.delete(id);

        verify(repository).deleteById(id);
    }
}
