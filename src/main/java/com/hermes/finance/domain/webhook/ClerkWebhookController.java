package com.hermes.finance.domain.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hermes.finance.domain.user.UserService;
import com.hermes.finance.dto.request.ClerkWebhookEvent;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SvixWebhookVerifier;
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
    private final SvixWebhookVerifier svixVerifier;

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    public ClerkWebhookController(UserService userService,
                                   ObjectMapper objectMapper,
                                   AppLogger appLogger,
                                   SvixWebhookVerifier svixVerifier) {
        this.userService   = userService;
        this.objectMapper  = objectMapper;
        this.appLogger     = appLogger;
        this.svixVerifier  = svixVerifier;
    }

    @PostMapping("/clerk")
    public ResponseEntity<Void> handleClerkWebhook(
            @RequestHeader("svix-id")        String svixId,
            @RequestHeader("svix-timestamp") String svixTimestamp,
            @RequestHeader("svix-signature") String svixSignature,
            @RequestBody String payload) {

        if (!svixVerifier.verify(webhookSecret, svixId, svixTimestamp, svixSignature, payload)) {
            appLogger.warn(LoggingConstants.INVALID_TOKEN,
                Map.of("reason", "invalid_svix_signature", "source", "clerk_webhook"));
            return ResponseEntity.badRequest().build();
        }

        try {
            ClerkWebhookEvent event = objectMapper.readValue(payload, ClerkWebhookEvent.class);

            switch (event.getType() != null ? event.getType() : "") {
                case "user.created" -> userService.createFromClerk(event.getData());
                case "user.updated" -> userService.updateFromClerk(event.getData());
                case "user.deleted" -> userService.anonymize(event.getData().getId());
                default -> { /* demais eventos do Clerk são ignorados */ }
            }
        } catch (Exception e) {
            appLogger.error(LoggingConstants.UNHANDLED_EXCEPTION,
                Map.of("path", "/api/webhooks/clerk"), e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }
}
