package com.hermes.finance.domain.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hermes.finance.domain.user.UserService;
import com.hermes.finance.domain.user.UserSyncData;
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

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final AppLogger appLogger;
    private final WebhookSignatureVerifier signatureVerifier;
    private final ClerkWebhookEventMapper eventMapper;
    private final String webhookSecret;

    public ClerkWebhookController(
        UserService userService,
        ObjectMapper objectMapper,
        AppLogger appLogger,
        WebhookSignatureVerifier signatureVerifier,
        ClerkWebhookEventMapper eventMapper,
        @Value("${clerk.webhook.secret}") String webhookSecret
    ) {
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.appLogger = appLogger;
        this.signatureVerifier = signatureVerifier;
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
        if (!signatureVerifier.verify(webhookSecret, svixId, svixTimestamp, svixSignature, payload)) {
            appLogger.warn(LoggingConstants.INVALID_TOKEN,
                Map.of("reason", "invalid_svix_signature", "source", "clerk_webhook"));
            return ResponseEntity.badRequest().build();
        }

        try {
            ClerkWebhookEvent event = objectMapper.readValue(payload, ClerkWebhookEvent.class);
            UserSyncData syncData = eventMapper.toUserSyncData(event.getData());

            switch (event.getType() != null ? event.getType() : "") {
                case "user.created" -> userService.createFromExternalProvider(syncData);
                case "user.updated" -> userService.updateFromExternalProvider(syncData);
                case "user.deleted" -> {
                    if (syncData != null) {
                        userService.anonymize(syncData.externalAuthId());
                    }
                }
                default -> {
                    // Ignora eventos nao utilizados no backend.
                }
            }
        } catch (Exception e) {
            appLogger.error(LoggingConstants.UNHANDLED_EXCEPTION,
                Map.of("path", "/api/webhooks/clerk"), e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }
}
