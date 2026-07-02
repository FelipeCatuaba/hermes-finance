package com.hermes.finance.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class AppLogger {

    private static final Logger log = LoggerFactory.getLogger(AppLogger.class);
    private static final Set<String> SENSITIVE_KEY_FRAGMENTS = Set.of(
        "email",
        "name",
        "password",
        "token",
        "amount",
        "description",
        "notes"
    );

    public void info(String event, Map<String, Object> context) {
        log.info(buildMessage(event, context));
    }

    public void warn(String event, Map<String, Object> context) {
        log.warn(buildMessage(event, context));
    }

    public void error(String event, Map<String, Object> context, Throwable ex) {
        Map<String, Object> safeContext = new LinkedHashMap<>();
        if (context != null) {
            safeContext.putAll(context);
        }
        safeContext.put("exception", ex != null ? ex.getClass().getSimpleName() : "unknown");
        log.error(buildMessage(event, safeContext));
    }

    String buildMessage(String event, Map<String, Object> context) {
        StringBuilder sb = new StringBuilder("event=").append(event);
        sanitize(context).forEach((k, v) -> sb.append(' ').append(k).append('=').append(v));
        return sb.toString();
    }

    private Map<String, Object> sanitize(Map<String, Object> context) {
        Map<String, Object> safeContext = new LinkedHashMap<>();
        if (context == null) {
            return safeContext;
        }

        context.forEach((key, value) -> {
            if (key != null && !isSensitive(key)) {
                safeContext.put(key, value);
            }
        });
        return safeContext;
    }

    private boolean isSensitive(String key) {
        String normalizedKey = key.toLowerCase();
        return SENSITIVE_KEY_FRAGMENTS.stream().anyMatch(normalizedKey::contains);
    }
}
