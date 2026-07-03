package com.hermes.finance.domain.share;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.ShareCreateRequest;
import com.hermes.finance.dto.response.ExpenseCategorySummaryResponse;
import com.hermes.finance.dto.response.PublicShareExpenseResponse;
import com.hermes.finance.dto.response.PublicShareResponse;
import com.hermes.finance.dto.response.ShareTokenResponse;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import com.hermes.finance.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ShareTokenService {

    private static final int DEFAULT_EXPIRATION_DAYS = 7;
    private static final int TOKEN_BYTES = 32;

    private final ShareTokenRepositoryPort repository;
    private final SecurityUtils securityUtils;
    private final AppLogger appLogger;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String publicBaseUrl;

    public ShareTokenService(ShareTokenRepositoryPort repository,
                             SecurityUtils securityUtils,
                             AppLogger appLogger,
                             @Value("${app.public-base-url:http://localhost:4200}") String publicBaseUrl) {
        this.repository = repository;
        this.securityUtils = securityUtils;
        this.appLogger = appLogger;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    @Transactional
    public ShareTokenResponse create(ShareCreateRequest request) {
        YearMonth period = validatePeriod(request.month(), request.year());
        User currentUser = securityUtils.getCurrentUser();
        if (!repository.familyMemberBelongsToUser(request.familyMemberId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Membro nao pertence ao usuario");
        }

        OffsetDateTime now = OffsetDateTime.now();
        repository.revokeActiveFor(currentUser.getId(), request.familyMemberId(), period.getMonthValue(), period.getYear(), now);

        String rawToken = generateRawToken();
        ShareToken token = new ShareToken();
        token.setId(UUID.randomUUID());
        token.setUserId(currentUser.getId());
        token.setFamilyMemberId(request.familyMemberId());
        token.setMonth(period.getMonthValue());
        token.setYear(period.getYear());
        token.setTokenHash(hash(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusDays(expirationDays(request.expiresInDays())));

        ShareToken saved = repository.save(token);
        appLogger.info(LoggingConstants.SHARE_LINK_CREATED, Map.of(
            "shareId", saved.getId(),
            "familyMemberId", saved.getFamilyMemberId(),
            "month", saved.getMonth(),
            "year", saved.getYear()
        ));
        return toResponse(saved, shareUrl(rawToken));
    }

    public List<ShareTokenResponse> list() {
        User currentUser = securityUtils.getCurrentUser();
        return repository.findByUserId(currentUser.getId())
            .stream()
            .map(token -> toResponse(token, null))
            .toList();
    }

    public void revoke(UUID id) {
        User currentUser = securityUtils.getCurrentUser();
        ShareToken token = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link nao encontrado"));
        if (!token.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado ao link informado");
        }

        repository.revokeById(id, currentUser.getId(), OffsetDateTime.now());
        appLogger.info(LoggingConstants.SHARE_LINK_REVOKED, Map.of(
            "shareId", id,
            "familyMemberId", token.getFamilyMemberId(),
            "month", token.getMonth(),
            "year", token.getYear()
        ));
    }

    public PublicShareResponse getPublicStatement(String rawToken) {
        ShareToken token = repository.findValidByHash(hash(rawToken), OffsetDateTime.now())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link nao encontrado"));

        YearMonth period = YearMonth.of(token.getYear(), token.getMonth());
        List<PublicShareExpenseItem> items = repository.findPublicExpenses(
            token.getUserId(),
            token.getFamilyMemberId(),
            period.atDay(1),
            period.plusMonths(1).atDay(1)
        );
        BigDecimal total = items.stream()
            .map(PublicShareExpenseItem::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        appLogger.info(LoggingConstants.SHARE_TOKEN_ACCESSED, Map.of(
            "shareId", token.getId(),
            "familyMemberId", token.getFamilyMemberId(),
            "month", token.getMonth(),
            "year", token.getYear()
        ));

        return new PublicShareResponse(
            token.getFamilyMemberName(),
            token.getFamilyMemberRelation(),
            token.getMonth(),
            token.getYear(),
            total,
            items.stream().map(this::toPublicExpense).toList()
        );
    }

    private ShareTokenResponse toResponse(ShareToken token, String shareUrl) {
        return new ShareTokenResponse(
            token.getId(),
            token.getFamilyMemberId(),
            token.getFamilyMemberName(),
            token.getFamilyMemberRelation(),
            token.getMonth(),
            token.getYear(),
            token.getExpiresAt(),
            token.getRevokedAt(),
            token.getCreatedAt(),
            shareUrl
        );
    }

    private PublicShareExpenseResponse toPublicExpense(PublicShareExpenseItem item) {
        ExpenseCategorySummaryResponse category = item.categoryId() == null ? null : new ExpenseCategorySummaryResponse(
            item.categoryId(),
            item.categoryName(),
            item.categoryIcon(),
            item.categoryColorHex()
        );
        return new PublicShareExpenseResponse(
            item.description(),
            item.amount(),
            item.expenseDate(),
            category,
            item.paymentMethod(),
            item.fixed()
        );
    }

    private YearMonth validatePeriod(int month, int year) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mes invalido");
        }
        if (year < 1900 || year > 9999) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ano invalido");
        }
        return YearMonth.of(year, month);
    }

    private int expirationDays(Integer expiresInDays) {
        return expiresInDays == null ? DEFAULT_EXPIRATION_DAYS : expiresInDays;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String shareUrl(String rawToken) {
        return publicBaseUrl + "/share/" + rawToken;
    }
}
