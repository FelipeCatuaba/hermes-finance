package com.hermes.finance.security;

import com.hermes.finance.dto.request.ChangePasswordRequest;
import com.hermes.finance.dto.request.LoginRequest;
import com.hermes.finance.dto.request.RefreshTokenRequest;
import com.hermes.finance.dto.request.RegisterRequest;
import com.hermes.finance.dto.response.AuthTokensResponse;
import com.hermes.finance.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private SecurityUtils securityUtils;
    @InjectMocks private AuthController authController;
    @Mock private HttpServletRequest httpServletRequest;

    @Test
    void shouldRegisterUsingForwardedIp() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@acme.com", "password123");
        AuthTokensResponse response = new AuthTokensResponse("a", "r", 900L);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 10.0.0.1");
        when(authService.register(request, "1.2.3.4")).thenReturn(response);

        AuthTokensResponse result = authController.register(request, httpServletRequest);

        assertEquals(response, result);
    }

    @Test
    void shouldLoginUsingRemoteAddrWhenNoForwardedHeader() {
        LoginRequest request = new LoginRequest("user@acme.com", "password123");
        AuthTokensResponse response = new AuthTokensResponse("a", "r", 900L);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(authService.login(request, "127.0.0.1")).thenReturn(response);

        AuthTokensResponse result = authController.login(request, httpServletRequest);

        assertEquals(response, result);
    }

    @Test
    void shouldLoginUsingRemoteAddrWhenForwardedHeaderIsBlank() {
        LoginRequest request = new LoginRequest("user@acme.com", "password123");
        AuthTokensResponse response = new AuthTokensResponse("a", "r", 900L);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("   ");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");
        when(authService.login(request, "127.0.0.2")).thenReturn(response);

        AuthTokensResponse result = authController.login(request, httpServletRequest);

        assertEquals(response, result);
    }

    @Test
    void shouldRefresh() {
        RefreshTokenRequest request = new RefreshTokenRequest("token");
        AuthTokensResponse response = new AuthTokensResponse("a", "r", 900L);
        when(authService.refresh(request)).thenReturn(response);

        assertEquals(response, authController.refresh(request));
    }

    @Test
    void shouldLogout() {
        RefreshTokenRequest request = new RefreshTokenRequest("token");

        authController.logout(request);

        verify(authService).logout(request);
    }

    @Test
    void shouldChangePasswordUsingCurrentUser() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass123");
        UUID userId = UUID.randomUUID();
        when(securityUtils.getCurrentUserIdOrAnonymous()).thenReturn(userId.toString());

        authController.changePassword(request);

        verify(authService).changePassword(userId, request);
    }
}
