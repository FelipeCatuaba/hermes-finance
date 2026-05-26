package com.hermes.finance.domain.family;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyMemberTest {

    @Test
    void shouldGetAndSetAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();

        FamilyMember member = new FamilyMember();
        member.setId(id);
        member.setUserId(userId);
        member.setName("Isa");
        member.setRelation("Filha");
        member.setActive(true);
        member.setCreatedAt(createdAt);

        assertThat(member.getId()).isEqualTo(id);
        assertThat(member.getUserId()).isEqualTo(userId);
        assertThat(member.getName()).isEqualTo("Isa");
        assertThat(member.getRelation()).isEqualTo("Filha");
        assertThat(member.isActive()).isTrue();
        assertThat(member.getCreatedAt()).isEqualTo(createdAt);
    }
}
