package com.hermes.finance.domain.share;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.ShareCreateRequest;
import com.hermes.finance.dto.response.PublicShareResponse;
import com.hermes.finance.dto.response.ShareTokenResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareTokenServiceTest {

    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID MEMBER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID SHARE_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Mock
    private ShareTokenRepositoryPort repository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AppLogger appLogger;

    private ShareTokenService service;

    @BeforeEach
    void setUp() {
        service = new ShareTokenService(repository, securityUtils, appLogger, "https://app.hermes.local/");
    }

    @Test
    void shouldCreateShareTokenRevokingPreviousLinkForSameMemberAndPeriod() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser());
        when(repository.familyMemberBelongsToUser(MEMBER_ID, USER_ID)).thenReturn(true);
        when(repository.save(any(ShareToken.class))).thenAnswer(invocation -> {
            ShareToken token = invocation.getArgument(0);
            token.setFamilyMemberName("Ana");
            token.setFamilyMemberRelation("Filha");
            return token;
        });

        ShareTokenResponse response = service.create(new ShareCreateRequest(MEMBER_ID, 3, 2026, 10));

        ArgumentCaptor<ShareToken> tokenCaptor = ArgumentCaptor.forClass(ShareToken.class);
        verify(repository).revokeActiveFor(eq(USER_ID), eq(MEMBER_ID), eq(3), eq(2026), any(OffsetDateTime.class));
        verify(repository).save(tokenCaptor.capture());
        ShareToken saved = tokenCaptor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(MEMBER_ID, saved.getFamilyMemberId());
        assertEquals(64, saved.getTokenHash().length());
        assertTrue(response.shareUrl().startsWith("https://app.hermes.local/share/"));
        assertFalse(response.shareUrl().contains(saved.getTokenHash()));
        assertEquals("Ana", response.familyMemberName());
        verify(appLogger).info(eq(LoggingConstants.SHARE_LINK_CREATED), any());
    }

    @Test
    void shouldRejectShareForMemberOwnedByAnotherUser() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser());
        when(repository.familyMemberBelongsToUser(MEMBER_ID, USER_ID)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.create(new ShareCreateRequest(MEMBER_ID, 3, 2026, null)));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnNotFoundForInvalidPublicToken() {
        when(repository.findValidByHash(any(), any())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.getPublicStatement("invalid-token"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldReturnPublicStatementWithoutOwnerData() {
        ShareToken token = token();
        when(repository.findValidByHash(any(), any())).thenReturn(Optional.of(token));
        when(repository.findPublicExpenses(eq(USER_ID), eq(MEMBER_ID), eq(LocalDate.of(2026, 3, 1)), eq(LocalDate.of(2026, 4, 1))))
            .thenReturn(List.of(new PublicShareExpenseItem(
                "Escola",
                new BigDecimal("120.50"),
                LocalDate.of(2026, 3, 10),
                UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                "Educacao",
                "school",
                "#3B82F6",
                "card",
                false
            )));

        PublicShareResponse response = service.getPublicStatement("raw-token");

        assertEquals("Ana", response.familyMemberName());
        assertEquals("Filha", response.familyMemberRelation());
        assertEquals(3, response.month());
        assertEquals(2026, response.year());
        assertEquals(new BigDecimal("120.50"), response.total());
        assertEquals(1, response.expenses().size());
        assertEquals("Escola", response.expenses().get(0).description());
        verify(appLogger).info(eq(LoggingConstants.SHARE_TOKEN_ACCESSED), eq(Map.of(
            "shareId", SHARE_ID,
            "familyMemberId", MEMBER_ID,
            "month", 3,
            "year", 2026
        )));
    }

    @Test
    void shouldRevokeOwnedShareLink() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser());
        when(repository.findById(SHARE_ID)).thenReturn(Optional.of(token()));

        service.revoke(SHARE_ID);

        verify(repository).revokeById(eq(SHARE_ID), eq(USER_ID), any(OffsetDateTime.class));
        verify(appLogger).info(eq(LoggingConstants.SHARE_LINK_REVOKED), any());
    }

    private static User currentUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setExternalAuthId("user_123");
        user.setEmail("owner@example.com");
        user.setName("Owner");
        return user;
    }

    private static ShareToken token() {
        ShareToken token = new ShareToken();
        token.setId(SHARE_ID);
        token.setUserId(USER_ID);
        token.setFamilyMemberId(MEMBER_ID);
        token.setFamilyMemberName("Ana");
        token.setFamilyMemberRelation("Filha");
        token.setMonth(3);
        token.setYear(2026);
        token.setTokenHash("hash");
        token.setCreatedAt(OffsetDateTime.now().minusDays(1));
        token.setExpiresAt(OffsetDateTime.now().plusDays(6));
        return token;
    }
}
