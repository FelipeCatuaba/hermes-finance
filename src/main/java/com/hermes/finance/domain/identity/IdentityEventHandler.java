package com.hermes.finance.domain.identity;

public interface IdentityEventHandler {

    void handle(IdentityUserEvent event);
}

