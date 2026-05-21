package com.hermes.finance.domain.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryWebhookReplayGuard implements WebhookReplayGuard {

    private final long maxTimestampSkewSeconds;
    private final long replayCacheTtlSeconds;
    private final Map<String, Long> seenWebhookIds = new ConcurrentHashMap<>();

    public InMemoryWebhookReplayGuard(
        @Value("${clerk.webhook.max-timestamp-skew-seconds:300}") long maxTimestampSkewSeconds,
        @Value("${clerk.webhook.replay-cache-ttl-seconds:900}") long replayCacheTtlSeconds
    ) {
        this.maxTimestampSkewSeconds = maxTimestampSkewSeconds;
        this.replayCacheTtlSeconds = replayCacheTtlSeconds;
    }

    @Override
    public boolean isValidTimestamp(String unixSecondsTimestamp) {
        try {
            long requestTs = Long.parseLong(unixSecondsTimestamp);
            long now = Instant.now().getEpochSecond();
            return Math.abs(now - requestTs) <= maxTimestampSkewSeconds;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    public boolean isFirstDelivery(String webhookMessageId) {
        if (webhookMessageId == null || webhookMessageId.isBlank()) {
            return false;
        }

        long now = Instant.now().getEpochSecond();
        long cutoff = now - replayCacheTtlSeconds;
        seenWebhookIds.entrySet().removeIf(entry -> entry.getValue() < cutoff);

        Long previous = seenWebhookIds.putIfAbsent(webhookMessageId, now);
        return previous == null;
    }
}
