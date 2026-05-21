package com.hermes.finance.domain.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hermes.finance.domain.identity.IdentityEventHandler;
import com.hermes.finance.dto.request.ClerkWebhookEvent;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class ClerkWebhookController {

    private final IdentityEventHandler identityEventHandler;
    private final ObjectMapper objectMapper;
    private final AppLogger appLogger;
    private final WebhookSignatureVerifier signatureVerifier;
    private final WebhookReplayGuard webhookReplayGuard;
    private final ClerkWebhookEventMapper eventMapper;
    private final String webhookSecret;

    public ClerkWebhookController(
        IdentityEventHandler identityEventHandler,
        ObjectMapper objectMapper,
        AppLogger appLogger,
        WebhookSignatureVerifier signatureVerifier,
        WebhookReplayGuard webhookReplayGuard,
        ClerkWebhookEventMapper eventMapper,
        @Value("${clerk.webhook.secret}") String webhookSecret
    ) {
        this.identityEventHandler = identityEventHandler;
        this.objectMapper = objectMapper;
        this.appLogger = appLogger;
        this.signatureVerifier = signatureVerifier;
        this.webhookReplayGuard = webhookReplayGuard;
        this.eventMapper = eventMapper;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/clerk")
    public ResponseEntity<Void> handleClerkWebhook(
        @RequestHeader("svix-id") String svixId,
        @RequestHeader("svix-timestamp") String svixTimestamp,
        @RequestHeader("svix-signature") String svixSignature,
        @RequestBody String payload
    ) {
        if (!webhookReplayGuard.isValidTimestamp(svixTimestamp)) {
            appLogger.warn(LoggingConstants.INVALID_TOKEN,
                Map.of("reason", "invalid_svix_timestamp", "source", "clerk_webhook"));
            return ResponseEntity.badRequest().build();
        }

        if (!webhookReplayGuard.isFirstDelivery(svixId)) {
            appLogger.warn(LoggingConstants.INVALID_TOKEN,
                Map.of("reason", "replayed_svix_id", "source", "clerk_webhook"));
            return ResponseEntity.ok().build();
        }

        if (!signatureVerifier.verify(webhookSecret, svixId, svixTimestamp, svixSignature, payload)) {
            appLogger.warn(LoggingConstants.INVALID_TOKEN,
                Map.of("reason", "invalid_svix_signature", "source", "clerk_webhook"));
            return ResponseEntity.badRequest().build();
        }

        try {
            ClerkWebhookEvent event = objectMapper.readValue(payload, ClerkWebhookEvent.class);
            identityEventHandler.handle(eventMapper.toIdentityUserEvent(event));
        } catch (Exception e) {
            appLogger.error(LoggingConstants.UNHANDLED_EXCEPTION,
                Map.of("path", "/api/webhooks/clerk"), e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }
}
