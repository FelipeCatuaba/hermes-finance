package com.hermes.finance.logging;

public final class LoggingConstants {

    private LoggingConstants() {
    }

    public static final String USER_REGISTERED = "USER_REGISTERED";
    public static final String USER_LOGIN_SUCCESS = "USER_LOGIN_SUCCESS";
    public static final String USER_LOGIN_FAILED = "USER_LOGIN_FAILED";
    public static final String RATE_LIMIT_HIT = "RATE_LIMIT_HIT";
    public static final String TOKEN_REFRESH = "TOKEN_REFRESH";
    public static final String USER_LOGOUT = "USER_LOGOUT";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";

    public static final String FORBIDDEN_ACCESS_ATTEMPT = "FORBIDDEN_ACCESS_ATTEMPT";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";

    public static final String EXPENSE_CREATED = "EXPENSE_CREATED";
    public static final String EXPENSE_UPDATED = "EXPENSE_UPDATED";
    public static final String EXPENSE_DELETED = "EXPENSE_DELETED";
    public static final String INSTALLMENT_GROUP_CREATED = "INSTALLMENT_GROUP_CREATED";

    public static final String INCOME_CREATED = "INCOME_CREATED";
    public static final String INCOME_UPDATED = "INCOME_UPDATED";
    public static final String INCOME_DELETED = "INCOME_DELETED";

    public static final String UNHANDLED_EXCEPTION = "UNHANDLED_EXCEPTION";
}
