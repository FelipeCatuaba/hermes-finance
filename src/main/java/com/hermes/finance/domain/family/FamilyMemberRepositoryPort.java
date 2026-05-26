package com.hermes.finance.domain.family;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FamilyMemberRepositoryPort {
    FamilyMember save(FamilyMember member);
    List<FamilyMember> findByUserId(UUID userId, boolean includeInactive);
    Optional<FamilyMember> findById(UUID id);
    FamilyMember update(FamilyMember member);
    void deactivate(UUID id);
}
