package com.hermes.finance.logging;

public final class LoggingConstants {

    private LoggingConstants() {
    }

    // Webhook — sincronização de usuários via Clerk
    public static final String USER_REGISTERED    = "USER_REGISTERED";
    public static final String USER_UPDATED       = "USER_UPDATED";
    public static final String USER_DELETED       = "USER_DELETED";

    // Segurança
    public static final String FORBIDDEN_ACCESS_ATTEMPT = "FORBIDDEN_ACCESS_ATTEMPT";
    public static final String INVALID_TOKEN            = "INVALID_TOKEN";

    // Gastos
    public static final String EXPENSE_CREATED          = "EXPENSE_CREATED";
    public static final String EXPENSE_UPDATED          = "EXPENSE_UPDATED";
    public static final String EXPENSE_DELETED          = "EXPENSE_DELETED";
    public static final String INSTALLMENT_GROUP_CREATED = "INSTALLMENT_GROUP_CREATED";

    // Receitas
    public static final String INCOME_CREATED    = "INCOME_CREATED";
    public static final String INCOME_UPDATED    = "INCOME_UPDATED";
    public static final String INCOME_DELETED    = "INCOME_DELETED";

    // Sistema
    public static final String UNHANDLED_EXCEPTION = "UNHANDLED_EXCEPTION";
}
