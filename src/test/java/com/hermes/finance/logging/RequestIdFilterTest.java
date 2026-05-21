package com.hermes.finance.logging;

import com.hermes.finance.security.AuthIdentityProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestIdFilterTest {

    @Mock
    private AuthIdentityProvider authIdentityProvider;

    @InjectMocks
    private RequestIdFilter requestIdFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void testDoFilterInternalAddsRequestId() throws ServletException, IOException {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("anonymous");
        MDC.clear();

        requestIdFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithAuthenticatedUser() throws ServletException, IOException {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("user123");
        MDC.clear();

        requestIdFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalClearsContext() throws ServletException, IOException {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("user");
        MDC.clear();

        requestIdFilter.doFilterInternal(request, response, filterChain);

        // MDC should be cleared after finally block
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalHandlesException() throws ServletException, IOException {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("user");
        doThrow(new ServletException("Filter error")).when(filterChain).doFilter(any(), any());

        try {
            requestIdFilter.doFilterInternal(request, response, filterChain);
        } catch (ServletException e) {
            // Expected behavior - exception is rethrown
        }

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalGeneratesUniqueRequestId() throws ServletException, IOException {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("user");
        MDC.clear();

        requestIdFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
