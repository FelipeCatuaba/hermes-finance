package com.hermes.finance.util;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityUtils securityUtils;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnClerkIdWhenAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user_2abc");
        when(authentication.getName()).thenReturn("user_2abc");
        SecurityContextHolder.setContext(securityContext);

        assertEquals("user_2abc", securityUtils.getCurrentClerkId());
    }

    @Test
    void shouldThrowUnauthorizedWhenClerkIdRequestedWithoutAuth() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            securityUtils::getCurrentClerkId
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldThrowUnauthorizedWhenClerkIdRequestedWhileNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            securityUtils::getCurrentClerkId
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldThrowUnauthorizedWhenClerkIdRequestedForAnonymousPrincipal() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.setContext(securityContext);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            securityUtils::getCurrentClerkId
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldReturnCurrentUserWhenSynchronized() {
        User user = new User();
        user.setClerkId("user_synced");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user_synced");
        when(authentication.getName()).thenReturn("user_synced");
        when(userRepository.findByClerkId("user_synced")).thenReturn(Optional.of(user));
        SecurityContextHolder.setContext(securityContext);

        assertEquals(user, securityUtils.getCurrentUser());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotSynchronized() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user_missing");
        when(authentication.getName()).thenReturn("user_missing");
        when(userRepository.findByClerkId("user_missing")).thenReturn(Optional.empty());
        SecurityContextHolder.setContext(securityContext);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            securityUtils::getCurrentUser
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testGetCurrentUserIdWhenAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user_2abc");
        when(authentication.getName()).thenReturn("user_2abc");

        SecurityContextHolder.setContext(securityContext);

        String userId = securityUtils.getCurrentUserIdOrAnonymous();
        assertEquals("user_2abc", userId);
    }

    @Test
    void testGetCurrentUserIdWhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContextHolder.setContext(securityContext);

        String userId = securityUtils.getCurrentUserIdOrAnonymous();
        assertEquals("anonymous", userId);
    }

    @Test
    void testGetCurrentUserIdWhenAnonymous() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        SecurityContextHolder.setContext(securityContext);

        String userId = securityUtils.getCurrentUserIdOrAnonymous();
        assertEquals("anonymous", userId);
    }

    @Test
    void testGetCurrentUserIdWhenAuthenticationNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        SecurityContextHolder.setContext(securityContext);

        String userId = securityUtils.getCurrentUserIdOrAnonymous();
        assertEquals("anonymous", userId);
    }
}
