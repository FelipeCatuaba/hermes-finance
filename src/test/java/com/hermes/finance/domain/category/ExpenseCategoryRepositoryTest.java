package com.hermes.finance.domain.category;

import com.hermes.finance.util.NativeQueryCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private NativeQueryCatalog nativeQueryCatalog;

    @Mock
    private ExpenseCategoryRowMapper rowMapper;

    @InjectMocks
    private ExpenseCategoryRepository repository;

    @Test
    void shouldSaveCategory() {
        when(nativeQueryCatalog.get("expenseCategory.insert")).thenReturn("insert");

        ExpenseCategory category = new ExpenseCategory();
        category.setUserId(UUID.randomUUID());
        category.setName("Pets");
        category.setIcon("shapes");
        category.setColorHex("#64748B");
        category.setDefault(false);
        category.setActive(true);

        ExpenseCategory saved = repository.save(category);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        verify(jdbcTemplate).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    void shouldFindVisibleAndById() {
        when(nativeQueryCatalog.get("expenseCategory.findVisibleForUser")).thenReturn("list");
        when(nativeQueryCatalog.get("expenseCategory.findById")).thenReturn("find");

        ExpenseCategory category = new ExpenseCategory();
        category.setId(UUID.randomUUID());
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(ExpenseCategoryRowMapper.class)))
            .thenReturn(List.of(category));

        List<ExpenseCategory> list = repository.findVisibleForUser(UUID.randomUUID(), true);
        Optional<ExpenseCategory> found = repository.findById(UUID.randomUUID());

        assertThat(list).hasSize(1);
        assertThat(found).isPresent();
    }

    @Test
    void shouldUpdateDeleteDeactivateAndCheckUsage() {
        when(nativeQueryCatalog.get("expenseCategory.update")).thenReturn("upd");
        when(nativeQueryCatalog.get("expenseCategory.delete")).thenReturn("del");
        when(nativeQueryCatalog.get("expenseCategory.deactivate")).thenReturn("deact");
        when(nativeQueryCatalog.get("expenseCategory.isInUse")).thenReturn("use");
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Integer.class)))
            .thenReturn(1);

        ExpenseCategory category = new ExpenseCategory();
        category.setId(UUID.randomUUID());
        category.setName("X");
        category.setIcon("i");
        category.setColorHex("#000000");
        category.setActive(true);

        repository.update(category);
        repository.deleteById(category.getId());
        repository.deactivate(category.getId());
        boolean inUse = repository.isCategoryInUse(category.getId());

        assertThat(inUse).isTrue();
    }
}
