package com.hermes.finance.security;

public interface AuthenticatedUserContext {

    String getRequiredUserId();

    String getUserIdOrAnonymous();
}

