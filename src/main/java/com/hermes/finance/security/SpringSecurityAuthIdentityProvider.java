package com.hermes.finance.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SpringSecurityAuthIdentityProvider implements AuthIdentityProvider {

    @Override
    public String getRequiredUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nao autenticado");
        }

        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nao autenticado");
        }
        return name;
    }

    @Override
    public String getUserIdOrAnonymous() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }

        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            return "anonymous";
        }
        return name;
    }
}
