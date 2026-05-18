package com.hermes.finance.security;

import com.hermes.finance.domain.auth.RefreshToken;
import com.hermes.finance.domain.auth.RefreshTokenRepository;
import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepository;
import com.hermes.finance.dto.request.LoginRequest;
import com.hermes.finance.dto.request.RefreshTokenRequest;
import com.hermes.finance.dto.request.RegisterRequest;
import com.hermes.finance.dto.request.ChangePasswordRequest;
import com.hermes.finance.dto.response.AuthTokensResponse;
import com.hermes.finance.exception.BusinessException;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final String BAD_CREDENTIALS_MESSAGE = "Credenciais invalidas";
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordService passwordService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppLogger appLogger;
    private final String pepper;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordService passwordService,
                       JwtTokenProvider jwtTokenProvider,
                       AppLogger appLogger,
                       @Value("${security.password.pepper}") String pepper,
                       @Value("${security.jwt.access-expiration-ms}") long accessExpirationMs,
                       @Value("${security.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordService = passwordService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.appLogger = appLogger;
        this.pepper = pepper;
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public AuthTokensResponse register(RegisterRequest request, String ip) {
        userRepository.findByEmail(request.email().trim().toLowerCase())
            .ifPresent(user -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Nao foi possivel concluir cadastro");
            });

        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setName(request.name().trim());
        user.setPassword(passwordService.hash(request.password() + pepper));
        user.setRole("OWNER");
        user.setActive(true);

        User saved = userRepository.save(user);
        appLogger.info(LoggingConstants.USER_REGISTERED, Map.of("ip", ip));
        return issueTokens(saved.getId());
    }

    @Transactional
    public AuthTokensResponse login(LoginRequest request, String ip) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> invalidCredentials(ip));

        boolean valid = passwordService.verify(request.password() + pepper, user.getPassword());
        if (!valid) {
            throw invalidCredentials(ip);
        }

        appLogger.info(LoggingConstants.USER_LOGIN_SUCCESS, Map.of("ip", ip));
        return issueTokens(user.getId());
    }

    @Transactional
    public AuthTokensResponse refresh(RefreshTokenRequest request) {
        String hashed = sha256(request.refreshToken());
        RefreshToken token = refreshTokenRepository.findActiveByToken(hashed)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS_MESSAGE));
        refreshTokenRepository.revokeById(token.getId());
        appLogger.info(LoggingConstants.TOKEN_REFRESH, Map.of("userId", token.getUserId().toString()));
        return issueTokens(token.getUserId());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String hashed = sha256(request.refreshToken());
        refreshTokenRepository.findActiveByToken(hashed).ifPresent(token -> {
            refreshTokenRepository.revokeById(token.getId());
            appLogger.info(LoggingConstants.USER_LOGOUT, Map.of("userId", token.getUserId().toString()));
        });
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS_MESSAGE));

        boolean validCurrent = passwordService.verify(request.currentPassword() + pepper, user.getPassword());
        if (!validCurrent) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
        }

        String newPasswordHash = passwordService.hash(request.newPassword() + pepper);
        userRepository.updatePassword(userId, newPasswordHash);
        refreshTokenRepository.revokeAllByUserId(userId);
        appLogger.info(LoggingConstants.PASSWORD_CHANGED, Map.of("userId", userId.toString()));
    }

    private AuthTokensResponse issueTokens(UUID userId) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String rawRefreshToken = generateOpaqueToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(sha256(rawRefreshToken));
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(OffsetDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        refreshTokenRepository.save(refreshToken);
        return new AuthTokensResponse(accessToken, rawRefreshToken, accessExpirationMs / 1000);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new BusinessException("Falha ao processar token");
        }
    }

    private ResponseStatusException invalidCredentials(String ip) {
        appLogger.warn(LoggingConstants.USER_LOGIN_FAILED, Map.of("ip", ip, "reason", "bad_credentials"));
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS_MESSAGE);
    }
}
