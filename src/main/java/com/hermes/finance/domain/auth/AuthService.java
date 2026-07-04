package com.hermes.finance.domain.auth;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepositoryPort;
import com.hermes.finance.dto.request.AuthLoginRequest;
import com.hermes.finance.dto.request.AuthRegisterRequest;
import com.hermes.finance.dto.response.AuthResponse;
import com.hermes.finance.dto.response.AuthUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshTokenTtlDays;

    public AuthService(
        UserRepositoryPort userRepository,
        RefreshTokenRepositoryPort refreshTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService,
        @Value("${security.jwt.refresh-token-ttl-days:30}") long refreshTokenTtlDays
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        String email = normalizeEmail(request.email());
        String name = request.name().trim();
        String passwordHash = passwordEncoder.encode(request.password());

        User user = userRepository.findByEmail(email)
            .map(existing -> claimExistingUser(existing, name, passwordHash))
            .orElseGet(() -> createUser(name, email, passwordHash));

        return issueSession(user);
    }

    @Transactional
    public AuthResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
            .orElseThrow(() -> unauthorized());
        if (!user.isActive() || user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw unauthorized();
        }
        return issueSession(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        String tokenHash = hash(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByHash(tokenHash)
            .orElseThrow(() -> unauthorized());
        if (stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw unauthorized();
        }

        User user = userRepository.findById(stored.getUserId())
            .filter(User::isActive)
            .orElseThrow(() -> unauthorized());
        refreshTokenRepository.revoke(tokenHash);
        return issueSession(user);
    }

    @Transactional
    public void logout(UUID userId, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshTokenRepository.revokeAllForUser(userId);
            return;
        }
        refreshTokenRepository.revoke(hash(refreshToken));
    }

    public AuthUserResponse toUserResponse(User user) {
        return new AuthUserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    private User claimExistingUser(User existing, String name, String passwordHash) {
        if (existing.getPasswordHash() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado");
        }
        userRepository.updateInternalCredentials(existing.getId(), name, passwordHash, "OWNER", true);
        existing.setName(name);
        existing.setPasswordHash(passwordHash);
        existing.setRole("OWNER");
        existing.setActive(true);
        return existing;
    }

    private User createUser(String name, String email, String passwordHash) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordHash);
        user.setRole("OWNER");
        user.setActive(true);
        return userRepository.save(user);
    }

    private AuthResponse issueSession(User user) {
        AuthToken accessToken = jwtTokenService.createAccessToken(user);
        String rawRefreshToken = generateRefreshToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(hash(rawRefreshToken));
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(refreshTokenTtlDays));
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(accessToken.value(), rawRefreshToken, accessToken.expiresInSeconds(), toUserResponse(user));
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Nao foi possivel gerar hash do refresh token", ex);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha invalidos");
    }
}
