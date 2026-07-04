package com.hermes.finance.util;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepository;
import com.hermes.finance.security.AuthIdentityProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {
    private static final UUID USER_ID = UUID.fromString("2e746853-e27f-4891-b196-35f7a9d527f4");

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthIdentityProvider authIdentityProvider;

    @InjectMocks
    private SecurityUtils securityUtils;

    @Test
    void shouldReturnExternalAuthIdWhenAuthenticated() {
        when(authIdentityProvider.getRequiredUserId()).thenReturn("user_2abc");

        assertEquals("user_2abc", securityUtils.getCurrentExternalAuthId());
    }

    @Test
    void shouldPropagateUnauthorizedWhenNotAuthenticated() {
        when(authIdentityProvider.getRequiredUserId())
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nao autenticado"));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            securityUtils::getCurrentExternalAuthId
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldReturnCurrentUserWhenSynchronized() {
        User user = new User();
        user.setId(USER_ID);

        when(authIdentityProvider.getRequiredUserId()).thenReturn(USER_ID.toString());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertEquals(user, securityUtils.getCurrentUser());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotSynchronized() {
        when(authIdentityProvider.getRequiredUserId()).thenReturn(USER_ID.toString());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            securityUtils::getCurrentUser
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldThrowUnauthorizedWhenTokenSubjectIsNotInternalUserId() {
        when(authIdentityProvider.getRequiredUserId()).thenReturn("user_missing");

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            securityUtils::getCurrentUser
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldReturnCurrentUserIdWhenAuthenticated() {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("user_2abc");

        String userId = securityUtils.getCurrentUserIdOrAnonymous();
        assertEquals("user_2abc", userId);
    }

    @Test
    void shouldReturnAnonymousWhenNotAuthenticated() {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("anonymous");

        String userId = securityUtils.getCurrentUserIdOrAnonymous();
        assertEquals("anonymous", userId);
    }
}
