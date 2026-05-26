package com.hermes.finance.domain.family;

import com.hermes.finance.dto.request.FamilyMemberUpsertRequest;
import com.hermes.finance.dto.response.FamilyMemberResponse;
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
class FamilyMemberControllerTest {

    @Mock
    private FamilyMemberService service;

    @InjectMocks
    private FamilyMemberController controller;

    @Test
    void shouldDelegateCrudOperations() {
        UUID id = UUID.randomUUID();
        FamilyMemberResponse response = new FamilyMemberResponse(id, "Isa", "Filha", true, OffsetDateTime.now());

        when(service.list(false)).thenReturn(List.of(response));
        when(service.create(new FamilyMemberUpsertRequest("Isa", "Filha"))).thenReturn(response);
        when(service.update(id, new FamilyMemberUpsertRequest("Isa", "Filha"))).thenReturn(response);

        assertThat(controller.list(false)).hasSize(1);
        assertThat(controller.create(new FamilyMemberUpsertRequest("Isa", "Filha")).name()).isEqualTo("Isa");
        assertThat(controller.update(id, new FamilyMemberUpsertRequest("Isa", "Filha")).id()).isEqualTo(id);
        controller.deactivate(id);

        verify(service).deactivate(id);
    }
}
