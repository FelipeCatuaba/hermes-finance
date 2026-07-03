package com.hermes.finance.domain.share;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ShareToken {

    private UUID id;
    private UUID userId;
    private UUID familyMemberId;
    private String familyMemberName;
    private String familyMemberRelation;
    private int month;
    private int year;
    private String tokenHash;
    private OffsetDateTime expiresAt;
    private OffsetDateTime revokedAt;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getFamilyMemberId() { return familyMemberId; }
    public void setFamilyMemberId(UUID familyMemberId) { this.familyMemberId = familyMemberId; }

    public String getFamilyMemberName() { return familyMemberName; }
    public void setFamilyMemberName(String familyMemberName) { this.familyMemberName = familyMemberName; }

    public String getFamilyMemberRelation() { return familyMemberRelation; }
    public void setFamilyMemberRelation(String familyMemberRelation) { this.familyMemberRelation = familyMemberRelation; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public OffsetDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(OffsetDateTime revokedAt) { this.revokedAt = revokedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
