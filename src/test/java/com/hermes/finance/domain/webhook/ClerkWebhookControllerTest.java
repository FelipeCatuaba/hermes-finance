package com.hermes.finance.domain.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hermes.finance.domain.identity.IdentityEventType;
import com.hermes.finance.domain.identity.IdentityEventHandler;
import com.hermes.finance.domain.identity.IdentityUserEvent;
import com.hermes.finance.dto.request.ClerkWebhookEvent;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClerkWebhookControllerTest {

    @Mock
    private IdentityEventHandler identityEventHandler;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AppLogger appLogger;

    @Mock
    private WebhookSignatureVerifier signatureVerifier;

    @Mock
    private WebhookReplayGuard webhookReplayGuard;

    @Mock
    private ClerkWebhookEventMapper eventMapper;

    private ClerkWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new ClerkWebhookController(
            identityEventHandler,
            objectMapper,
            appLogger,
            signatureVerifier,
            webhookReplayGuard,
            eventMapper,
            "secret"
        );
    }

    @Test
    void shouldReturnBadRequestWhenSignatureIsInvalid() {
        when(signatureVerifier.verify(any(), any(), any(), any(), any())).thenReturn(false);
        when(webhookReplayGuard.isValidTimestamp("1710000000")).thenReturn(true);
        when(webhookReplayGuard.isFirstDelivery("id")).thenReturn(true);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "1710000000", "sig", "{}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(appLogger).warn(eq(LoggingConstants.INVALID_TOKEN), any());
        verify(identityEventHandler, never()).handle(any());
    }

    @Test
    void shouldCreateUserOnUserCreatedEvent() throws Exception {
        String payload = "{\"type\":\"user.created\"}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setId("user_1");
        event.setType("user.created");
        event.setData(data);

        when(signatureVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(webhookReplayGuard.isValidTimestamp("1710000000")).thenReturn(true);
        when(webhookReplayGuard.isFirstDelivery("id")).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);
        when(eventMapper.toIdentityUserEvent(event))
            .thenReturn(new IdentityUserEvent(IdentityEventType.USER_CREATED, null));

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "1710000000", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(identityEventHandler).handle(any());
    }

    @Test
    void shouldAnonymizeUserOnUserDeletedEvent() throws Exception {
        String payload = "{\"type\":\"user.deleted\"}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setId("user_del");
        event.setType("user.deleted");
        event.setData(data);

        when(signatureVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(webhookReplayGuard.isValidTimestamp("1710000000")).thenReturn(true);
        when(webhookReplayGuard.isFirstDelivery("id")).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);
        when(eventMapper.toIdentityUserEvent(event))
            .thenReturn(new IdentityUserEvent(IdentityEventType.USER_DELETED, null));

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "1710000000", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(identityEventHandler).handle(any());
    }

    @Test
    void shouldUpdateUserOnUserUpdatedEvent() throws Exception {
        String payload = "{\"type\":\"user.updated\"}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setId("user_upd");
        event.setType("user.updated");
        event.setData(data);

        when(signatureVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(webhookReplayGuard.isValidTimestamp("1710000000")).thenReturn(true);
        when(webhookReplayGuard.isFirstDelivery("id")).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);
        when(eventMapper.toIdentityUserEvent(event))
            .thenReturn(new IdentityUserEvent(IdentityEventType.USER_UPDATED, null));

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "1710000000", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(identityEventHandler).handle(any());
    }

    @Test
    void shouldIgnoreUnsupportedEventTypes() throws Exception {
        String payload = "{\"type\":\"session.created\"}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        event.setType("session.created");

        when(signatureVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(webhookReplayGuard.isValidTimestamp("1710000000")).thenReturn(true);
        when(webhookReplayGuard.isFirstDelivery("id")).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);
        when(eventMapper.toIdentityUserEvent(event))
            .thenReturn(new IdentityUserEvent(IdentityEventType.UNSUPPORTED, null));

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "1710000000", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(identityEventHandler).handle(any());
    }

    @Test
    void shouldTreatNullEventTypeAsEmpty() throws Exception {
        String payload = "{\"type\":null}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();

        when(signatureVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(webhookReplayGuard.isValidTimestamp("1710000000")).thenReturn(true);
        when(webhookReplayGuard.isFirstDelivery("id")).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);
        when(eventMapper.toIdentityUserEvent(event))
            .thenReturn(new IdentityUserEvent(IdentityEventType.UNSUPPORTED, null));

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "1710000000", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(identityEventHandler).handle(any());
    }

    @Test
    void shouldReturnInternalServerErrorWhenPayloadParsingFails() throws Exception {
        String payload = "not-json";
        when(signatureVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(webhookReplayGuard.isValidTimestamp("1710000000")).thenReturn(true);
        when(webhookReplayGuard.isFirstDelivery("id")).thenReturn(true);
        doThrow(new RuntimeException("parse error"))
            .when(objectMapper).readValue(payload, ClerkWebhookEvent.class);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "1710000000", "sig", payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(appLogger).error(eq(LoggingConstants.UNHANDLED_EXCEPTION), any(), any(RuntimeException.class));
    }

    @Test
    void shouldReturnBadRequestWhenTimestampInvalid() {
        when(webhookReplayGuard.isValidTimestamp("bad-ts")).thenReturn(false);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "bad-ts", "sig", "{}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(signatureVerifier, never()).verify(any(), any(), any(), any(), any());
    }

    @Test
    void shouldIgnoreReplayEvent() {
        when(webhookReplayGuard.isValidTimestamp("1710000000")).thenReturn(true);
        when(webhookReplayGuard.isFirstDelivery("id")).thenReturn(false);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "1710000000", "sig", "{}");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(signatureVerifier, never()).verify(any(), any(), any(), any(), any());
    }
}
