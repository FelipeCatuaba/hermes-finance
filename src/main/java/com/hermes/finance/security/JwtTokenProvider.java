package com.hermes.finance.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final String secret;

    public JwtTokenProvider(@Value("${security.jwt.secret:dev-secret-for-tests}") String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }
}
