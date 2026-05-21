package com.hermes.finance.util;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepositoryPort;
import com.hermes.finance.security.AuthIdentityProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SecurityUtils {

    private final UserRepositoryPort userRepository;
    private final AuthIdentityProvider authIdentityProvider;

    public SecurityUtils(UserRepositoryPort userRepository, AuthIdentityProvider authIdentityProvider) {
        this.userRepository = userRepository;
        this.authIdentityProvider = authIdentityProvider;
    }

    /**
     * Retorna o external auth id (claim sub do JWT) do usuario autenticado.
     */
    public String getCurrentExternalAuthId() {
        return authIdentityProvider.getRequiredUserId();
    }

    /**
     * Retorna o usuario local correspondente ao id externo do token JWT.
     * Lanca 404 se o usuario ainda nao foi sincronizado via webhook.
     */
    public User getCurrentUser() {
        String externalAuthId = getCurrentExternalAuthId();
        return userRepository.findByExternalAuthId(externalAuthId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuario nao sincronizado - aguarde o webhook do provedor de autenticacao"));
    }

    /**
     * Retorna o id externo ou "anonymous" para logs.
     */
    public String getCurrentUserIdOrAnonymous() {
        return authIdentityProvider.getUserIdOrAnonymous();
    }
}
