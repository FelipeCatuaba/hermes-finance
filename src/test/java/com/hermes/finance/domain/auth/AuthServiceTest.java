package com.hermes.finance.domain.auth;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepositoryPort;
import com.hermes.finance.dto.request.AuthLoginRequest;
import com.hermes.finance.dto.request.AuthRegisterRequest;
import com.hermes.finance.dto.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final UUID USER_ID = UUID.fromString("52c5d6ec-8f6b-4717-8b84-4f9d3f7dbdd3");

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Test
    void shouldRegisterNewUserAndIssueSession() {
        when(userRepository.findByEmail("felipe@hermes.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Senha123!")).thenReturn("argon2-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(USER_ID);
            return user;
        });
        when(jwtTokenService.createAccessToken(any(User.class))).thenReturn(new AuthToken("access-token", 900));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService().register(new AuthRegisterRequest(" Felipe ", " FELIPE@HERMES.COM ", "Senha123!"));

        assertEquals("access-token", response.accessToken());
        assertThat(response.refreshToken()).isNotBlank();
        assertEquals(USER_ID, response.user().id());
        assertEquals("felipe@hermes.com", response.user().email());
        assertEquals("Felipe", response.user().name());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("argon2-hash", userCaptor.getValue().getPasswordHash());
        assertEquals("OWNER", userCaptor.getValue().getRole());
        assertThat(userCaptor.getValue().isActive()).isTrue();

        ArgumentCaptor<RefreshToken> refreshCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshCaptor.capture());
        assertEquals(USER_ID, refreshCaptor.getValue().getUserId());
        assertThat(refreshCaptor.getValue().getTokenHash()).hasSize(64);
        assertThat(refreshCaptor.getValue().getExpiresAt()).isAfter(OffsetDateTime.now());
    }

    @Test
    void shouldRejectLoginWhenCredentialsAreInvalid() {
        when(userRepository.findByEmail("missing@hermes.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> authService().login(new AuthLoginRequest("missing@hermes.com", "wrong", true))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldRotateRefreshToken() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("felipe@hermes.com");
        user.setName("Felipe");
        user.setPasswordHash("argon2-hash");
        user.setRole("OWNER");
        user.setActive(true);

        RefreshToken stored = new RefreshToken();
        stored.setUserId(USER_ID);
        stored.setTokenHash(sha256("raw-refresh-token"));
        stored.setExpiresAt(OffsetDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByHash(sha256("raw-refresh-token"))).thenReturn(Optional.of(stored));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(jwtTokenService.createAccessToken(user)).thenReturn(new AuthToken("new-access-token", 900));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService().refresh("raw-refresh-token");

        assertEquals("new-access-token", response.accessToken());
        verify(refreshTokenRepository).revoke(sha256("raw-refresh-token"));

        ArgumentCaptor<RefreshToken> refreshCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshCaptor.capture());
        assertEquals(USER_ID, refreshCaptor.getValue().getUserId());
        assertThat(refreshCaptor.getValue().getTokenHash()).isNotEqualTo(sha256("raw-refresh-token"));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private AuthService authService() {
        return new AuthService(userRepository, refreshTokenRepository, passwordEncoder, jwtTokenService, 30);
    }
}
