package com.hermes.finance.domain.category;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseCategoryTest {

    @Test
    void shouldGetAndSetAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();

        ExpenseCategory category = new ExpenseCategory();
        category.setId(id);
        category.setUserId(userId);
        category.setName("Educação");
        category.setIcon("school");
        category.setColorHex("#123456");
        category.setDefault(true);
        category.setActive(true);
        category.setCreatedAt(createdAt);

        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getUserId()).isEqualTo(userId);
        assertThat(category.getName()).isEqualTo("Educação");
        assertThat(category.getIcon()).isEqualTo("school");
        assertThat(category.getColorHex()).isEqualTo("#123456");
        assertThat(category.isDefault()).isTrue();
        assertThat(category.isActive()).isTrue();
        assertThat(category.getCreatedAt()).isEqualTo(createdAt);
    }
}
