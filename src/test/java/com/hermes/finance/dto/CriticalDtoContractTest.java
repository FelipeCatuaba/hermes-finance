package com.hermes.finance.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hermes.finance.dto.request.BudgetUpsertRequest;
import com.hermes.finance.dto.request.ExpenseInstallmentCreateRequest;
import com.hermes.finance.dto.request.ExpenseCreateRequest;
import com.hermes.finance.dto.request.FamilyMemberUpsertRequest;
import com.hermes.finance.dto.request.ImportExpenseRequest;
import com.hermes.finance.dto.request.ImportIncomeRequest;
import com.hermes.finance.dto.request.IncomeUpsertRequest;
import com.hermes.finance.dto.request.ShareCreateRequest;
import com.hermes.finance.dto.response.BudgetResponse;
import com.hermes.finance.dto.response.BudgetStatusResponse;
import com.hermes.finance.dto.response.ExpenseBulkCreateResponse;
import com.hermes.finance.dto.response.ExpenseListResponse;
import com.hermes.finance.dto.response.ExpenseResponse;
import com.hermes.finance.dto.response.FamilyMemberResponse;
import com.hermes.finance.dto.response.ImportBatchResponse;
import com.hermes.finance.dto.response.IncomeResponse;
import com.hermes.finance.dto.response.MonthlyReportResponse;
import com.hermes.finance.dto.response.OpenInstallmentsReportResponse;
import com.hermes.finance.dto.response.PublicShareResponse;
import com.hermes.finance.dto.response.ShareTokenResponse;
import com.hermes.finance.dto.response.YearlyReportResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CriticalDtoContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Set<String> FORBIDDEN_PUBLIC_DTO_FIELDS = Set.of(
        "userId",
        "password",
        "role",
        "refreshToken",
        "accessToken"
    );
    private static final UUID ID = UUID.fromString("379ab046-4034-4f4a-a455-5868dde8f5bb");
    private static final UUID CATEGORY_ID = UUID.fromString("2c34ed5d-8d9f-4e7f-87e3-a34be90d60cb");
    private static final UUID MEMBER_ID = UUID.fromString("8bd15f7b-6cf3-4a95-8bc7-d244807e4620");

    @Test
    void shouldIgnoreForgedInternalFieldsOnExpenseCreateRequest() throws Exception {
        String payload = """
            {
              "description": "Mercado",
              "amount": 120.50,
              "expenseDate": "2026-03-10",
              "categoryId": "%s",
              "familyMemberId": "%s",
              "paymentMethod": "card",
              "notes": "safe note",
              "isFixed": true,
              "userId": "62212dbc-38db-4bfa-a986-d3a2c560420a",
              "scope": "family",
              "role": "ADMIN",
              "password": "secret",
              "refreshToken": "rt"
            }
            """.formatted(CATEGORY_ID, MEMBER_ID);

        ExpenseCreateRequest request = OBJECT_MAPPER.readValue(payload, ExpenseCreateRequest.class);

        assertEquals("Mercado", request.description());
        assertEquals(new BigDecimal("120.50"), request.amount());
        assertEquals(LocalDate.of(2026, 3, 10), request.expenseDate());
        assertEquals(CATEGORY_ID, request.categoryId());
        assertEquals(MEMBER_ID, request.familyMemberId());
    }

    @Test
    void shouldIgnoreForgedInternalFieldsOnIncomeUpsertRequest() throws Exception {
        String payload = """
            {
              "description": "Salario",
              "amount": 8000.00,
              "incomeDate": "2026-03-05",
              "categoryId": "%s",
              "isRecurring": true,
              "notes": "safe note",
              "userId": "62212dbc-38db-4bfa-a986-d3a2c560420a",
              "role": "ADMIN",
              "password": "secret",
              "refreshToken": "rt"
            }
            """.formatted(CATEGORY_ID);

        IncomeUpsertRequest request = OBJECT_MAPPER.readValue(payload, IncomeUpsertRequest.class);

        assertEquals("Salario", request.description());
        assertEquals(new BigDecimal("8000.00"), request.amount());
        assertEquals(LocalDate.of(2026, 3, 5), request.incomeDate());
        assertEquals(CATEGORY_ID, request.categoryId());
    }

    @Test
    void shouldKeepCriticalRequestDtosFreeOfInternalIdentityAndCredentialFields() {
        List<Class<?>> requestDtos = List.of(
            ExpenseCreateRequest.class,
            ExpenseInstallmentCreateRequest.class,
            IncomeUpsertRequest.class,
            BudgetUpsertRequest.class,
            FamilyMemberUpsertRequest.class,
            ImportExpenseRequest.class,
            ImportIncomeRequest.class,
            ShareCreateRequest.class
        );

        requestDtos.forEach(this::assertRecordDoesNotDeclareForbiddenFields);
    }

    @Test
    void shouldKeepCriticalResponseDtosFreeOfInternalIdentityAndCredentialFields() {
        List<Class<?>> responseDtos = List.of(
            ExpenseResponse.class,
            IncomeResponse.class,
            BudgetResponse.class,
            BudgetStatusResponse.class,
            BudgetStatusResponse.Item.class,
            FamilyMemberResponse.class,
            MonthlyReportResponse.class,
            MonthlyReportResponse.IncomeSection.class,
            MonthlyReportResponse.IncomeItem.class,
            MonthlyReportResponse.OwnerExpensesSection.class,
            MonthlyReportResponse.CategoryBreakdown.class,
            MonthlyReportResponse.FamilyExpensesSection.class,
            MonthlyReportResponse.MemberBreakdown.class,
            MonthlyReportResponse.Summary.class,
            YearlyReportResponse.class,
            YearlyReportResponse.MonthSummary.class,
            OpenInstallmentsReportResponse.class,
            OpenInstallmentsReportResponse.Group.class,
            OpenInstallmentsReportResponse.FutureInstallment.class,
            ShareTokenResponse.class,
            PublicShareResponse.class,
            ExpenseListResponse.class,
            ExpenseBulkCreateResponse.class,
            ExpenseBulkCreateResponse.ItemError.class,
            ImportBatchResponse.class
        );

        responseDtos.forEach(this::assertRecordDoesNotDeclareForbiddenFields);
    }

    @Test
    void shouldNotSerializeInternalIdentityOrCredentialFieldsInCriticalResponses() throws Exception {
        JsonNode expense = OBJECT_MAPPER.valueToTree(new ExpenseResponse(
            ID,
            "Mercado",
            new BigDecimal("120.50"),
            LocalDate.of(2026, 3, 10),
            CATEGORY_ID,
            MEMBER_ID,
            null,
            null,
            null,
            "card",
            "safe note",
            true,
            "family",
            OffsetDateTime.parse("2026-03-10T10:15:30Z"),
            OffsetDateTime.parse("2026-03-10T10:15:30Z")
        ));
        JsonNode income = OBJECT_MAPPER.valueToTree(new IncomeResponse(
            ID,
            "Salario",
            new BigDecimal("8000.00"),
            LocalDate.of(2026, 3, 5),
            CATEGORY_ID,
            true,
            "safe note",
            OffsetDateTime.parse("2026-03-05T10:15:30Z"),
            OffsetDateTime.parse("2026-03-05T10:15:30Z")
        ));

        assertNoInternalFields(expense);
        assertNoInternalFields(income);
    }

    private void assertNoInternalFields(JsonNode node) {
        assertFalse(node.has("userId"));
        assertFalse(node.has("role"));
        assertFalse(node.has("password"));
        assertFalse(node.has("refreshToken"));
        assertFalse(node.has("accessToken"));
    }

    private void assertRecordDoesNotDeclareForbiddenFields(Class<?> type) {
        for (RecordComponent component : type.getRecordComponents()) {
            assertFalse(FORBIDDEN_PUBLIC_DTO_FIELDS.contains(component.getName()),
                () -> type.getSimpleName() + " exposes forbidden field " + component.getName());
        }
    }
}
