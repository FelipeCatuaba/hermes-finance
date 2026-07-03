package com.hermes.finance.domain.share;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShareTokenRepositoryPort {
    void revokeActiveFor(UUID userId, UUID familyMemberId, int month, int year, OffsetDateTime revokedAt);
    ShareToken save(ShareToken token);
    List<ShareToken> findByUserId(UUID userId);
    Optional<ShareToken> findById(UUID id);
    void revokeById(UUID id, UUID userId, OffsetDateTime revokedAt);
    Optional<ShareToken> findValidByHash(String tokenHash, OffsetDateTime now);
    List<PublicShareExpenseItem> findPublicExpenses(UUID userId, UUID familyMemberId, LocalDate startDate, LocalDate endDate);
    boolean familyMemberBelongsToUser(UUID familyMemberId, UUID userId);
}
