package com.hermes.finance.util;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepositoryPort;
import com.hermes.finance.security.AuthenticatedUserContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SecurityUtils {

    private final UserRepositoryPort userRepository;
    private final AuthenticatedUserContext authenticatedUserContext;

    public SecurityUtils(UserRepositoryPort userRepository, AuthenticatedUserContext authenticatedUserContext) {
        this.userRepository = userRepository;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    /**
     * Retorna o external auth id (claim sub do JWT) do usuário autenticado.
     */
    public String getCurrentExternalAuthId() {
        String userId = authenticatedUserContext.getRequiredUserId();
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado");
        }
        return userId;
    }

    /**
     * Retorna o usuário local correspondente ao id externo do token JWT.
     * Lança 404 se o usuário ainda não foi sincronizado via webhook.
     */
    public User getCurrentUser() {
        String externalAuthId = getCurrentExternalAuthId();
        return userRepository.findByExternalAuthId(externalAuthId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuário não sincronizado - aguarde o webhook do Clerk"));
    }

    /**
     * Retorna o id externo ou "anonymous" para logs.
     */
    public String getCurrentUserIdOrAnonymous() {
        return authenticatedUserContext.getUserIdOrAnonymous();
    }
}
