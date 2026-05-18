package com.hermes.finance.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityUtils securityUtils;

    @Test
    void testGetCurrentUserIdWhenAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user123");
        when(authentication.getName()).thenReturn("user123");
        
        SecurityContextHolder.setContext(securityContext);
        
        String userId = securityUtils.getCurrentUserIdOrAnonymous();
        assertEquals("user123", userId);
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
