package com.hermes.finance.security;

import com.hermes.finance.domain.auth.RefreshToken;
import com.hermes.finance.domain.auth.RefreshTokenRepository;
import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepository;
import com.hermes.finance.dto.request.ChangePasswordRequest;
import com.hermes.finance.dto.request.LoginRequest;
import com.hermes.finance.dto.request.RefreshTokenRequest;
import com.hermes.finance.dto.request.RegisterRequest;
import com.hermes.finance.dto.response.AuthTokensResponse;
import com.hermes.finance.logging.AppLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordService passwordService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AppLogger appLogger;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, passwordService,
            jwtTokenProvider, appLogger, "pepper", 900000L, 604800000L);
    }

    @Test
    void shouldRegisterAndIssueTokens() {
        RegisterRequest request = new RegisterRequest(" Alice ", " USER@ACME.COM ", "password123");
        User saved = new User();
        UUID userId = UUID.randomUUID();
        saved.setId(userId);

        when(userRepository.findByEmail("user@acme.com")).thenReturn(Optional.empty());
        when(passwordService.hash("password123pepper")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtTokenProvider.generateAccessToken(userId)).thenReturn("access-token");

        AuthTokensResponse response = authService.register(request, "127.0.0.1");

        assertEquals("access-token", response.accessToken());
        assertNotNull(response.refreshToken());
        assertEquals(900L, response.expiresIn());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldFailRegisterWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("Alice", "user@acme.com", "password123");
        when(userRepository.findByEmail("user@acme.com")).thenReturn(Optional.of(new User()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.register(request, "127.0.0.1"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void shouldLoginAndIssueTokens() {
        LoginRequest request = new LoginRequest(" USER@ACME.COM ", "password123");
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setPassword("hash");

        when(userRepository.findByEmail("user@acme.com")).thenReturn(Optional.of(user));
        when(passwordService.verify("password123pepper", "hash")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(userId)).thenReturn("access-token");

        AuthTokensResponse response = authService.login(request, "127.0.0.1");

        assertEquals("access-token", response.accessToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldFailLoginWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest("user@acme.com", "bad-password");
        User user = new User();
        user.setPassword("hash");
        when(userRepository.findByEmail("user@acme.com")).thenReturn(Optional.of(user));
        when(passwordService.verify("bad-passwordpepper", "hash")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.login(request, "127.0.0.1"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldFailLoginWhenUserIsNotFound() {
        when(userRepository.findByEmail("missing@acme.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.login(new LoginRequest("missing@acme.com", "password"), "127.0.0.1"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldRefreshToken() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        RefreshToken token = new RefreshToken();
        token.setId(tokenId);
        token.setUserId(userId);

        when(refreshTokenRepository.findActiveByToken(any())).thenReturn(Optional.of(token));
        when(jwtTokenProvider.generateAccessToken(userId)).thenReturn("access-token");

        AuthTokensResponse response = authService.refresh(new RefreshTokenRequest("raw-token"));

        assertEquals("access-token", response.accessToken());
        verify(refreshTokenRepository).revokeById(tokenId);
    }

    @Test
    void shouldIgnoreLogoutWhenTokenDoesNotExist() {
        when(refreshTokenRepository.findActiveByToken(any())).thenReturn(Optional.empty());

        authService.logout(new RefreshTokenRequest("raw-token"));

        verify(refreshTokenRepository, never()).revokeById(any());
    }

    @Test
    void shouldLogoutWhenTokenExists() {
        RefreshToken token = new RefreshToken();
        UUID tokenId = UUID.randomUUID();
        token.setId(tokenId);
        token.setUserId(UUID.randomUUID());
        when(refreshTokenRepository.findActiveByToken(any())).thenReturn(Optional.of(token));

        authService.logout(new RefreshTokenRequest("raw-token"));

        verify(refreshTokenRepository).revokeById(tokenId);
    }

    @Test
    void shouldChangePasswordAndRevokeTokens() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setPassword("old-hash");
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordService.verify("oldPasspepper", "old-hash")).thenReturn(true);
        when(passwordService.hash("newPass123pepper")).thenReturn("new-hash");

        authService.changePassword(userId, request);

        verify(userRepository).updatePassword(userId, "new-hash");
        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }

    @Test
    void shouldFailChangePasswordWhenCurrentPasswordIsInvalid() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setPassword("old-hash");
        ChangePasswordRequest request = new ChangePasswordRequest("wrong", "newPass123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordService.verify("wrongpepper", "old-hash")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.changePassword(userId, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldFailChangePasswordWhenUserIsMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.changePassword(userId, new ChangePasswordRequest("old", "newPass123")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldFailRefreshWhenTokenNotFound() {
        when(refreshTokenRepository.findActiveByToken(any())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.refresh(new RefreshTokenRequest("missing-token")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
