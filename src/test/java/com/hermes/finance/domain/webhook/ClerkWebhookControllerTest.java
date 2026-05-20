package com.hermes.finance.domain.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hermes.finance.domain.user.UserService;
import com.hermes.finance.dto.request.ClerkWebhookEvent;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SvixWebhookVerifier;
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
    private UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AppLogger appLogger;

    @Mock
    private SvixWebhookVerifier svixVerifier;

    private ClerkWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new ClerkWebhookController(userService, objectMapper, appLogger, svixVerifier);
    }

    @Test
    void shouldReturnBadRequestWhenSignatureIsInvalid() {
        when(svixVerifier.verify(any(), any(), any(), any(), any())).thenReturn(false);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "ts", "sig", "{}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(appLogger).warn(eq(LoggingConstants.INVALID_TOKEN), any());
        verify(userService, never()).createFromClerk(any());
    }

    @Test
    void shouldCreateUserOnUserCreatedEvent() throws Exception {
        String payload = "{\"type\":\"user.created\"}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setId("user_1");
        event.setType("user.created");
        event.setData(data);

        when(svixVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "ts", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).createFromClerk(data);
    }

    @Test
    void shouldAnonymizeUserOnUserDeletedEvent() throws Exception {
        String payload = "{\"type\":\"user.deleted\"}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setId("user_del");
        event.setType("user.deleted");
        event.setData(data);

        when(svixVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "ts", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).anonymize("user_del");
    }

    @Test
    void shouldUpdateUserOnUserUpdatedEvent() throws Exception {
        String payload = "{\"type\":\"user.updated\"}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setId("user_upd");
        event.setType("user.updated");
        event.setData(data);

        when(svixVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "ts", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).updateFromClerk(data);
    }

    @Test
    void shouldIgnoreUnsupportedEventTypes() throws Exception {
        String payload = "{\"type\":\"session.created\"}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        event.setType("session.created");

        when(svixVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "ts", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, never()).createFromClerk(any());
        verify(userService, never()).updateFromClerk(any());
        verify(userService, never()).anonymize(any());
    }

    @Test
    void shouldTreatNullEventTypeAsEmpty() throws Exception {
        String payload = "{\"type\":null}";
        ClerkWebhookEvent event = new ClerkWebhookEvent();

        when(svixVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        when(objectMapper.readValue(payload, ClerkWebhookEvent.class)).thenReturn(event);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "ts", "sig", payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, never()).createFromClerk(any());
    }

    @Test
    void shouldReturnInternalServerErrorWhenPayloadParsingFails() throws Exception {
        String payload = "not-json";
        when(svixVerifier.verify(any(), any(), any(), any(), eq(payload))).thenReturn(true);
        doThrow(new RuntimeException("parse error"))
            .when(objectMapper).readValue(payload, ClerkWebhookEvent.class);

        ResponseEntity<Void> response = controller.handleClerkWebhook("id", "ts", "sig", payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(appLogger).error(eq(LoggingConstants.UNHANDLED_EXCEPTION), any(), any(RuntimeException.class));
    }
}
