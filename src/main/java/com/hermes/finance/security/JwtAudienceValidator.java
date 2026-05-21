package com.hermes.finance.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Set;

public class JwtAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_AUDIENCE = new OAuth2Error(
        "invalid_token",
        "The required audience is missing",
        null
    );

    private final Set<String> allowedAudiences;

    public JwtAudienceValidator(Set<String> allowedAudiences) {
        this.allowedAudiences = allowedAudiences;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> tokenAudiences = jwt.getAudience();
        if (tokenAudiences == null || tokenAudiences.isEmpty()) {
            return OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE);
        }

        boolean hasAnyAllowedAudience = tokenAudiences.stream().anyMatch(allowedAudiences::contains);
        return hasAnyAllowedAudience
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE);
    }
}
