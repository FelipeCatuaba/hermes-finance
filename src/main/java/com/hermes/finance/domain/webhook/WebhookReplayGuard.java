package com.hermes.finance.domain.webhook;

public interface WebhookReplayGuard {

    boolean isValidTimestamp(String unixSecondsTimestamp);

    boolean isFirstDelivery(String webhookMessageId);
}
