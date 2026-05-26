package com.hermes.finance.domain.family;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.FamilyMemberUpsertRequest;
import com.hermes.finance.dto.response.FamilyMemberResponse;
import com.hermes.finance.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class FamilyMemberService {

    private final FamilyMemberRepositoryPort repository;
    private final SecurityUtils securityUtils;

    public FamilyMemberService(FamilyMemberRepositoryPort repository, SecurityUtils securityUtils) {
        this.repository = repository;
        this.securityUtils = securityUtils;
    }

    public List<FamilyMemberResponse> list(boolean includeInactive) {
        User currentUser = securityUtils.getCurrentUser();
        return repository.findByUserId(currentUser.getId(), includeInactive)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public FamilyMemberResponse create(FamilyMemberUpsertRequest request) {
        validateName(request.name());

        User currentUser = securityUtils.getCurrentUser();
        FamilyMember member = new FamilyMember();
        member.setUserId(currentUser.getId());
        member.setName(request.name().trim());
        member.setRelation(trimToNull(request.relation()));
        member.setActive(true);

        return toResponse(repository.save(member));
    }

    public FamilyMemberResponse update(UUID id, FamilyMemberUpsertRequest request) {
        validateName(request.name());
        FamilyMember existing = requireOwnedMember(id);

        existing.setName(request.name().trim());
        existing.setRelation(trimToNull(request.relation()));

        return toResponse(repository.update(existing));
    }

    public void deactivate(UUID id) {
        requireOwnedMember(id);
        repository.deactivate(id);
    }

    private FamilyMember requireOwnedMember(UUID id) {
        FamilyMember member = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membro não encontrado"));

        User currentUser = securityUtils.getCurrentUser();
        if (!member.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado ao membro informado");
        }

        return member;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }

        if (name.trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome deve ter no máximo 100 caracteres");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private FamilyMemberResponse toResponse(FamilyMember member) {
        return new FamilyMemberResponse(
            member.getId(),
            member.getName(),
            member.getRelation(),
            member.isActive(),
            member.getCreatedAt()
        );
    }
}
