package com.hermes.finance.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final int maxRequests;
    private final long windowSeconds;
    private final Map<String, Deque<Long>> requestWindowByKey = new ConcurrentHashMap<>();

    public IpRateLimitFilter(
        @Value("${security.rate-limit.max-requests:120}") int maxRequests,
        @Value("${security.rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/api/webhooks/") || path.startsWith("/api/public/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String key = clientIp(request) + "|" + request.getRequestURI();
        long now = Instant.now().getEpochSecond();
        Deque<Long> timestamps = requestWindowByKey.computeIfAbsent(key, ignored -> new ConcurrentLinkedDeque<>());

        synchronized (timestamps) {
            long cutoff = now - windowSeconds;
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= maxRequests) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Too many requests\"}");
                return;
            }
            timestamps.addLast(now);
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
