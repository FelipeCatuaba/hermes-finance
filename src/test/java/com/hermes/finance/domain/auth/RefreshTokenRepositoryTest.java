package com.hermes.finance.domain.auth;

import com.hermes.finance.util.NativeQueryCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryTest {

    @Mock private NamedParameterJdbcTemplate jdbcTemplate;
    @Mock private NativeQueryCatalog nativeQueryCatalog;
    @Mock private RefreshTokenRowMapper rowMapper;

    private RefreshTokenRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RefreshTokenRepository(jdbcTemplate, nativeQueryCatalog, rowMapper);
    }

    @Test
    void shouldSaveAndPopulateMissingFields() {
        RefreshToken token = new RefreshToken();
        token.setUserId(UUID.randomUUID());
        token.setToken("hashed");
        token.setExpiresAt(OffsetDateTime.now().plusDays(1));
        when(nativeQueryCatalog.get("refreshToken.insert")).thenReturn("insert");

        RefreshToken saved = repository.save(token);

        assertSame(token, saved);
        assertTrue(saved.getId() != null);
        assertTrue(saved.getCreatedAt() != null);
        verify(jdbcTemplate).update(eq("insert"), any(SqlParameterSource.class));
    }

    @Test
    void shouldFindActiveByToken() {
        RefreshToken token = new RefreshToken();
        when(nativeQueryCatalog.get("refreshToken.findActiveByToken")).thenReturn("find");
        when(jdbcTemplate.query(eq("find"), any(SqlParameterSource.class), eq(rowMapper))).thenReturn(List.of(token));

        Optional<RefreshToken> found = repository.findActiveByToken("hash");

        assertTrue(found.isPresent());
        assertSame(token, found.get());
    }

    @Test
    void shouldReturnEmptyWhenNoActiveToken() {
        when(nativeQueryCatalog.get("refreshToken.findActiveByToken")).thenReturn("find");
        when(jdbcTemplate.query(eq("find"), any(SqlParameterSource.class), eq(rowMapper))).thenReturn(List.of());

        Optional<RefreshToken> found = repository.findActiveByToken("hash");

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldRevokeById() {
        when(nativeQueryCatalog.get("refreshToken.revokeById")).thenReturn("revoke");
        repository.revokeById(UUID.randomUUID());
        verify(jdbcTemplate).update(eq("revoke"), any(SqlParameterSource.class));
    }

    @Test
    void shouldRevokeAllByUserId() {
        when(nativeQueryCatalog.get("refreshToken.revokeAllByUserId")).thenReturn("revokeAll");
        repository.revokeAllByUserId(UUID.randomUUID());
        verify(jdbcTemplate).update(eq("revokeAll"), any(SqlParameterSource.class));
    }
}
