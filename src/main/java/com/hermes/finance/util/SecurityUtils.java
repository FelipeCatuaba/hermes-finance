package com.hermes.finance.util;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepositoryPort;
import com.hermes.finance.security.AuthIdentityProvider;
import java.util.UUID;

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
     * Retorna o id interno (claim sub do JWT) do usuario autenticado.
     */
    public String getCurrentUserId() {
        return authIdentityProvider.getRequiredUserId();
    }

    /**
     * Compatibilidade com chamadas antigas enquanto o corte do provider externo termina.
     */
    public String getCurrentExternalAuthId() {
        return getCurrentUserId();
    }

    /**
     * Retorna o usuario local correspondente ao id interno do token JWT.
     */
    public User getCurrentUser() {
        String userId = getCurrentUserId();
        UUID internalUserId;
        try {
            internalUserId = UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de autenticacao invalido");
        }
        return userRepository.findById(internalUserId)
            .filter(User::isActive)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Usuario autenticado nao encontrado ou inativo"));
    }

    /**
     * Retorna o id externo ou "anonymous" para logs.
     */
    public String getCurrentUserIdOrAnonymous() {
        return authIdentityProvider.getUserIdOrAnonymous();
    }
}
