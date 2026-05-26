package com.hermes.finance.domain.category;

import com.hermes.finance.dto.request.ExpenseCategoryUpsertRequest;
import com.hermes.finance.dto.response.ExpenseCategoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryControllerTest {

    @Mock
    private ExpenseCategoryService service;

    @InjectMocks
    private ExpenseCategoryController controller;

    @Test
    void shouldDelegateCrudOperations() {
        UUID id = UUID.randomUUID();
        ExpenseCategoryResponse response = new ExpenseCategoryResponse(
            id, "Lazer", "party-popper", "#A855F7", false, true, OffsetDateTime.now()
        );

        when(service.list(false)).thenReturn(List.of(response));
        when(service.create(new ExpenseCategoryUpsertRequest("Lazer", "party-popper", "#A855F7"))).thenReturn(response);
        when(service.update(id, new ExpenseCategoryUpsertRequest("Lazer", "party-popper", "#A855F7"))).thenReturn(response);

        assertThat(controller.list(false)).hasSize(1);
        assertThat(controller.create(new ExpenseCategoryUpsertRequest("Lazer", "party-popper", "#A855F7")).name())
            .isEqualTo("Lazer");
        assertThat(controller.update(id, new ExpenseCategoryUpsertRequest("Lazer", "party-popper", "#A855F7")).id())
            .isEqualTo(id);
        controller.delete(id);

        verify(service).delete(id);
    }
}
