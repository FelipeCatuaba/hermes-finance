package com.hermes.finance.domain.family;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.FamilyMemberUpsertRequest;
import com.hermes.finance.dto.response.FamilyMemberResponse;
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
class FamilyMemberServiceTest {

    @Mock
    private FamilyMemberRepositoryPort repository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private FamilyMemberService service;

    @Test
    void shouldCreateMember() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(securityUtils.getCurrentUser()).thenReturn(user);

        FamilyMember saved = new FamilyMember();
        saved.setId(UUID.randomUUID());
        saved.setUserId(user.getId());
        saved.setName("Isa");
        saved.setRelation("Filha");
        saved.setActive(true);
        when(repository.save(any(FamilyMember.class))).thenReturn(saved);

        FamilyMemberResponse response = service.create(new FamilyMemberUpsertRequest(" Isa ", " Filha "));

        assertThat(response.name()).isEqualTo("Isa");
        assertThat(response.relation()).isEqualTo("Filha");
    }

    @Test
    void shouldThrowBadRequestWhenNameInvalid() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.create(new FamilyMemberUpsertRequest(" ", "X")));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldListMembersFromCurrentUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(securityUtils.getCurrentUser()).thenReturn(user);

        FamilyMember member = new FamilyMember();
        member.setId(UUID.randomUUID());
        member.setUserId(user.getId());
        member.setName("Amanda");
        member.setActive(true);
        when(repository.findByUserId(user.getId(), true)).thenReturn(List.of(member));

        List<FamilyMemberResponse> responses = service.list(true);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("Amanda");
    }

    @Test
    void shouldForbidUpdateWhenMemberFromAnotherUser() {
        UUID memberId = UUID.randomUUID();
        User owner = new User();
        owner.setId(UUID.randomUUID());
        when(securityUtils.getCurrentUser()).thenReturn(owner);

        FamilyMember member = new FamilyMember();
        member.setId(memberId);
        member.setUserId(UUID.randomUUID());
        when(repository.findById(memberId)).thenReturn(Optional.of(member));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.update(memberId, new FamilyMemberUpsertRequest("Novo", null)));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldDeactivateOwnedMember() {
        UUID memberId = UUID.randomUUID();
        User owner = new User();
        owner.setId(UUID.randomUUID());
        when(securityUtils.getCurrentUser()).thenReturn(owner);

        FamilyMember member = new FamilyMember();
        member.setId(memberId);
        member.setUserId(owner.getId());
        when(repository.findById(memberId)).thenReturn(Optional.of(member));

        service.deactivate(memberId);

        verify(repository).deactivate(memberId);
    }
}
