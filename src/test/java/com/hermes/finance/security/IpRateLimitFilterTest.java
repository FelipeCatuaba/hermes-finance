package com.hermes.finance.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpRateLimitFilterTest {

    @Test
    void shouldSkipNonSensitivePaths() throws ServletException, IOException {
        IpRateLimitFilter filter = new IpRateLimitFilter(1, 60);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/private/data");
        req.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        assertEquals(200, res.getStatus());
    }

    @Test
    void shouldRateLimitWebhookPath() throws ServletException, IOException {
        IpRateLimitFilter filter = new IpRateLimitFilter(1, 60);

        MockHttpServletRequest req1 = new MockHttpServletRequest("POST", "/api/webhooks/clerk");
        req1.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, new MockFilterChain());
        assertEquals(200, res1.getStatus());

        MockHttpServletRequest req2 = new MockHttpServletRequest("POST", "/api/webhooks/clerk");
        req2.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, new MockFilterChain());

        assertEquals(429, res2.getStatus());
        assertEquals("{\"message\":\"Too many requests\"}", res2.getContentAsString());
    }

    @Test
    void shouldUseForwardedIpAndDifferentPathBuckets() throws ServletException, IOException {
        IpRateLimitFilter filter = new IpRateLimitFilter(1, 60);

        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/public/a");
        req1.addHeader("X-Forwarded-For", "1.2.3.4, 9.9.9.9");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, new MockFilterChain());
        assertEquals(200, res1.getStatus());

        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/public/b");
        req2.addHeader("X-Real-IP", "1.2.3.4");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, new MockFilterChain());
        assertEquals(200, res2.getStatus());
    }

    @Test
    void shouldFallbackToRealIpWhenForwardedForIsBlank() throws ServletException, IOException {
        IpRateLimitFilter filter = new IpRateLimitFilter(1, 60);

        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/public/a");
        req1.addHeader("X-Forwarded-For", "   ");
        req1.addHeader("X-Real-IP", "5.6.7.8");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, new MockFilterChain());
        assertEquals(200, res1.getStatus());

        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/public/a");
        req2.addHeader("X-Real-IP", "5.6.7.8");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, new MockFilterChain());
        assertEquals(429, res2.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDiscardExpiredWindowEntries() throws Exception {
        IpRateLimitFilter filter = new IpRateLimitFilter(1, 60);
        Field mapField = IpRateLimitFilter.class.getDeclaredField("requestWindowByKey");
        mapField.setAccessible(true);
        Map<String, Deque<Long>> map = (Map<String, Deque<Long>>) mapField.get(filter);
        Deque<Long> deque = new ConcurrentLinkedDeque<>();
        deque.add(Instant.now().getEpochSecond() - 3600);
        map.put("9.9.9.9|/api/public/a", deque);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/public/a");
        req.setRemoteAddr("9.9.9.9");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());

        assertEquals(200, res.getStatus());
    }
}
