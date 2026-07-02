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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
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

        verify(response).setHeader(eq("X-Request-Id"), startsWith("req-"));
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("userId"));
    }

    @Test
    void testDoFilterInternalWithAuthenticatedUser() throws ServletException, IOException {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("user123");
        MDC.clear();

        requestIdFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response).setHeader(eq("X-Request-Id"), startsWith("req-"));
    }

    @Test
    void testDoFilterInternalClearsContext() throws ServletException, IOException {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("user");
        MDC.clear();

        requestIdFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("userId"));
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
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("userId"));
    }

    @Test
    void testDoFilterInternalGeneratesUniqueRequestId() throws ServletException, IOException {
        when(authIdentityProvider.getUserIdOrAnonymous()).thenReturn("user");
        MDC.clear();
        List<String> requestIds = new ArrayList<>();
        doAnswer(invocation -> {
            requestIds.add(MDC.get("requestId"));
            assertTrue(MDC.get("requestId").startsWith("req-"));
            assertTrue(MDC.get("userId").equals("user"));
            return null;
        }).when(filterChain).doFilter(request, response);

        requestIdFilter.doFilterInternal(request, response, filterChain);
        requestIdFilter.doFilterInternal(request, response, filterChain);

        assertNotEquals(requestIds.get(0), requestIds.get(1));
    }
}
