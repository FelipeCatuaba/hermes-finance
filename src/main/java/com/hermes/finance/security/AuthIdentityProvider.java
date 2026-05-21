package com.hermes.finance.security;

public interface AuthIdentityProvider {

    String getRequiredUserId();

    String getUserIdOrAnonymous();
}
