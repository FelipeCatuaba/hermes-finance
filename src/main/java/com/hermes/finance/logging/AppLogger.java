package com.hermes.finance.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AppLogger {

    private static final Logger log = LoggerFactory.getLogger(AppLogger.class);

    public void info(String event, Map<String, Object> context) {
        log.info(buildMessage(event, context));
    }

    public void warn(String event, Map<String, Object> context) {
        log.warn(buildMessage(event, context));
    }

    public void error(String event, Map<String, Object> context, Throwable ex) {
        context.put("exception", ex != null ? ex.getClass().getSimpleName() : "unknown");
        log.error(buildMessage(event, context));
    }

    private String buildMessage(String event, Map<String, Object> context) {
        StringBuilder sb = new StringBuilder("event=").append(event);
        context.forEach((k, v) -> sb.append(' ').append(k).append('=').append(v));
        return sb.toString();
    }
}
