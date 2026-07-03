package com.hermes.finance.exception;

import com.hermes.finance.dto.response.ApiErrorResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private AppLogger appLogger;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Test
    void testHandleBusinessException() {
        when(request.getRequestURI()).thenReturn("/api/test");
        BusinessException ex = new BusinessException("Business operation failed");

        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleBusiness(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("business_error", response.getBody().error());
        assertEquals("Business operation failed", response.getBody().message());
    }

    @Test
    void testHandleValidationException() {
        when(request.getRequestURI()).thenReturn("/api/submit");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);

        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleValidation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("validation_error", response.getBody().error());
    }

    @Test
    void testHandleGenericException() {
        when(request.getRequestURI()).thenReturn("/api/process");
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleAny(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("internal_error", response.getBody().error());
        verify(appLogger).error(anyString(), anyMap(), any(Exception.class));
    }

    @Test
    void testHandleConstraintViolationException() {
        when(request.getRequestURI()).thenReturn("/api/data");
        ConstraintViolationException ex = mock(ConstraintViolationException.class);

        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleValidation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleIllegalArgumentException() {
        when(request.getRequestURI()).thenReturn("/api/invalid");
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");

        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleValidation(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleResponseStatusException() {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");

        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleStatus(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("request_error", response.getBody().error());
        assertEquals("Credenciais invalidas", response.getBody().message());
    }

    @Test
    void shouldReturnGenericErrorForResponseStatusServerErrors() {
        when(request.getRequestURI()).thenReturn("/api/process");
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database password leaked");

        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleStatus(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("internal_error", response.getBody().error());
        assertEquals("Erro interno", response.getBody().message());
        verify(appLogger).error(eq(LoggingConstants.UNHANDLED_EXCEPTION), anyMap(), eq(ex));
    }

    @Test
    void shouldLogForbiddenAccessAttempt() {
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/expenses/123");
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");

        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleStatus(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(appLogger).warn(eq(LoggingConstants.FORBIDDEN_ACCESS_ATTEMPT), eq(java.util.Map.of(
            "method", "DELETE",
            "path", "/api/expenses/123",
            "status", 403
        )));
    }
}
