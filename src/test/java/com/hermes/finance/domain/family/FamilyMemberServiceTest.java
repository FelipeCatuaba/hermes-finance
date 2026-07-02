package com.hermes.finance.domain.family;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.FamilyMemberUpsertRequest;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyMemberServiceTest {

    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID MEMBER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Mock
    private FamilyMemberRepositoryPort repository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AppLogger appLogger;

    private FamilyMemberService service;

    @BeforeEach
    void setUp() {
        service = new FamilyMemberService(repository, securityUtils, appLogger);
    }

    @Test
    void shouldLogFamilyMemberCreatedWithoutSensitiveFields() {
        User user = currentUser();
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(repository.save(org.mockito.ArgumentMatchers.any(FamilyMember.class))).thenReturn(member());

        service.create(new FamilyMemberUpsertRequest("Ana", "Filha"));

        verify(appLogger).info(eq(LoggingConstants.FAMILY_MEMBER_CREATED), eq(Map.of("familyMemberId", MEMBER_ID)));
    }

    @Test
    void shouldLogFamilyMemberUpdatedWithoutSensitiveFields() {
        FamilyMember existing = member();
        when(repository.findById(MEMBER_ID)).thenReturn(Optional.of(existing));
        when(securityUtils.getCurrentUser()).thenReturn(currentUser());
        when(repository.update(existing)).thenReturn(existing);

        service.update(MEMBER_ID, new FamilyMemberUpsertRequest("Ana Maria", "Filha"));

        verify(appLogger).info(eq(LoggingConstants.FAMILY_MEMBER_UPDATED), eq(Map.of("familyMemberId", MEMBER_ID)));
    }

    @Test
    void shouldLogFamilyMemberDeactivatedWithoutSensitiveFields() {
        when(repository.findById(MEMBER_ID)).thenReturn(Optional.of(member()));
        when(securityUtils.getCurrentUser()).thenReturn(currentUser());

        service.deactivate(MEMBER_ID);

        verify(appLogger).info(eq(LoggingConstants.FAMILY_MEMBER_DEACTIVATED), eq(Map.of("familyMemberId", MEMBER_ID)));
    }

    private static User currentUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setExternalAuthId("user_external_id");
        user.setEmail("user@example.com");
        user.setName("Maria");
        return user;
    }

    private static FamilyMember member() {
        FamilyMember member = new FamilyMember();
        member.setId(MEMBER_ID);
        member.setUserId(USER_ID);
        member.setName("Ana");
        member.setRelation("Filha");
        member.setActive(true);
        member.setCreatedAt(OffsetDateTime.now());
        return member;
    }
}
