package com.hermes.finance.domain.auth;

import com.hermes.finance.domain.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtTokenService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long accessTokenTtlSeconds;

    public JwtTokenService(
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder,
        @Value("${security.jwt.access-token-ttl-seconds:900}") long accessTokenTtlSeconds
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public AuthToken createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenTtlSeconds);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("hermes-finance")
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("role", user.getRole())
            .claim("token_type", "access")
            .build();
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        return new AuthToken(jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue(), accessTokenTtlSeconds);
    }

    public Jwt decodeAccessToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        if (!"access".equals(jwt.getClaimAsString("token_type"))) {
            throw new JwtException("Token type invalido");
        }
        return jwt;
    }
}
