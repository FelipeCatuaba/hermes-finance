package com.hermes.finance.security;

import com.hermes.finance.logging.AppLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRateLimitFilterTest {

    @Mock private AppLogger appLogger;
    @InjectMocks private AuthRateLimitFilter filter;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @Test
    void shouldFilterLoginEndpoint() {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void shouldFilterRegisterEndpoint() {
        when(request.getRequestURI()).thenReturn("/api/auth/register");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterOtherEndpoints() {
        when(request.getRequestURI()).thenReturn("/api/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldAllowRequestWhenBucketHasCapacity() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/register");
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.1.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(appLogger, never()).warn(anyString(), anyMap());
    }

    @Test
    void shouldUseRemoteAddressWhenForwardedHeaderIsBlank() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/register");
        when(request.getHeader("X-Forwarded-For")).thenReturn("   ");
        when(request.getRemoteAddr()).thenReturn("127.0.0.9");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReturnTooManyRequestsWhenRateLimitIsExceeded() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.10.10.10");
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        for (int i = 0; i < 6; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(response).setStatus(429);
        verify(appLogger).warn(anyString(), anyMap());
    }
}
