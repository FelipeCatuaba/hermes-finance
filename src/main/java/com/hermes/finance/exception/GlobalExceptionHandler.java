package com.hermes.finance.exception;

import com.hermes.finance.dto.response.ApiErrorResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AppLogger appLogger;

    public GlobalExceptionHandler(AppLogger appLogger) {
        this.appLogger = appLogger;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
            new ApiErrorResponse("business_error", ex.getMessage(), Instant.now(), request.getRequestURI())
        );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
            new ApiErrorResponse("validation_error", "Requisicao invalida", Instant.now(), request.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(Exception ex, HttpServletRequest request) {
        appLogger.error(LoggingConstants.UNHANDLED_EXCEPTION,
            Map.of("path", request.getRequestURI()), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ApiErrorResponse("internal_error", "Erro interno", Instant.now(), request.getRequestURI())
        );
    }
}
