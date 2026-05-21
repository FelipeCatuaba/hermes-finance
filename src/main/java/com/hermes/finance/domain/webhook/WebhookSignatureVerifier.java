package com.hermes.finance.domain.webhook;

public interface WebhookSignatureVerifier {

    boolean verify(String secret, String id, String timestamp, String signature, String payload);
}

