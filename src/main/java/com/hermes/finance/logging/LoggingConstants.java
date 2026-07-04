package com.hermes.finance.logging;

public final class LoggingConstants {

    private LoggingConstants() {
    }

    // Usuarios
    public static final String USER_REGISTERED = "USER_REGISTERED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String USER_DELETED = "USER_DELETED";

    // Seguranca
    public static final String FORBIDDEN_ACCESS_ATTEMPT = "FORBIDDEN_ACCESS_ATTEMPT";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";

    // Gastos
    public static final String EXPENSE_CREATED = "EXPENSE_CREATED";
    public static final String EXPENSE_UPDATED = "EXPENSE_UPDATED";
    public static final String EXPENSE_DELETED = "EXPENSE_DELETED";
    public static final String EXPENSE_BULK_IMPORTED = "EXPENSE_BULK_IMPORTED";
    public static final String INSTALLMENT_GROUP_CREATED = "INSTALLMENT_GROUP_CREATED";
    public static final String IMPORT_COMPLETED = "IMPORT_COMPLETED";

    // Receitas
    public static final String INCOME_CREATED = "INCOME_CREATED";
    public static final String INCOME_UPDATED = "INCOME_UPDATED";
    public static final String INCOME_DELETED = "INCOME_DELETED";

    // Orcamentos
    public static final String BUDGET_UPDATED = "BUDGET_UPDATED";
    public static final String BUDGET_DELETED = "BUDGET_DELETED";

    // Compartilhamento familiar
    public static final String FAMILY_MEMBER_CREATED = "FAMILY_MEMBER_CREATED";
    public static final String FAMILY_MEMBER_UPDATED = "FAMILY_MEMBER_UPDATED";
    public static final String FAMILY_MEMBER_DEACTIVATED = "FAMILY_MEMBER_DEACTIVATED";
    public static final String SHARE_LINK_CREATED = "SHARE_LINK_CREATED";
    public static final String SHARE_LINK_REVOKED = "SHARE_LINK_REVOKED";
    public static final String SHARE_TOKEN_ACCESSED = "SHARE_TOKEN_ACCESSED";

    // Sistema
    public static final String UNHANDLED_EXCEPTION = "UNHANDLED_EXCEPTION";
}
