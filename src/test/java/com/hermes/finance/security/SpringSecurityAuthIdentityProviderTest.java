package com.hermes.finance.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringSecurityAuthIdentityProviderTest {

    private final SpringSecurityAuthIdentityProvider provider = new SpringSecurityAuthIdentityProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnRequiredUserIdWhenAuthenticated() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("user-1", "n/a");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("user-1", provider.getRequiredUserId());
        assertEquals("user-1", provider.getUserIdOrAnonymous());
    }

    @Test
    void shouldThrowUnauthorizedWhenAnonymousOrMissing() {
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, provider::getRequiredUserId);
        assertEquals(HttpStatus.UNAUTHORIZED, ex1.getStatusCode());

        TestingAuthenticationToken anon = new TestingAuthenticationToken("anonymousUser", "n/a");
        anon.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(anon);
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, provider::getRequiredUserId);
        assertEquals(HttpStatus.UNAUTHORIZED, ex2.getStatusCode());
    }

    @Test
    void shouldReturnAnonymousForInvalidContexts() {
        assertEquals("anonymous", provider.getUserIdOrAnonymous());

        TestingAuthenticationToken anon = new TestingAuthenticationToken("anonymousUser", "n/a");
        anon.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(anon);
        assertEquals("anonymous", provider.getUserIdOrAnonymous());
    }

    @Test
    void shouldTreatBlankNameAsAnonymousAndUnauthorized() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("", "n/a");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, provider::getRequiredUserId);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("anonymous", provider.getUserIdOrAnonymous());
    }

    @Test
    void shouldTreatNotAuthenticatedAsAnonymousAndUnauthorized() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("user-1", "n/a");
        auth.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, provider::getRequiredUserId);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("anonymous", provider.getUserIdOrAnonymous());
    }

    @Test
    void shouldTreatMissingNameAsAnonymousAndUnauthorized() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("principal");
        when(auth.getName()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, provider::getRequiredUserId);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("anonymous", provider.getUserIdOrAnonymous());
    }
}
