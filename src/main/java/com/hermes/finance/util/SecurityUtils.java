package com.hermes.finance.util;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.domain.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retorna o clerk_id (campo "sub" do JWT emitido pelo Clerk) do usuário autenticado.
     */
    public String getCurrentClerkId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado");
        }
        return auth.getName(); // getName() retorna o "sub" do JWT
    }

    /**
     * Retorna o usuário local correspondente ao clerk_id do token JWT.
     * Lança 404 se o usuário ainda não foi sincronizado via webhook.
     */
    public User getCurrentUser() {
        String clerkId = getCurrentClerkId();
        return userRepository.findByClerkId(clerkId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuário não sincronizado — aguarde o webhook do Clerk"));
    }

    /**
     * Retorna o clerk_id ou "anonymous" — usado para logging no MDC sem lançar exceção.
     */
    public String getCurrentUserIdOrAnonymous() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        return auth.getName();
    }
}
