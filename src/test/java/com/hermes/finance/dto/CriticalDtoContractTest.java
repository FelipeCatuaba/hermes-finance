package com.hermes.finance.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hermes.finance.dto.request.ExpenseCreateRequest;
import com.hermes.finance.dto.request.IncomeUpsertRequest;
import com.hermes.finance.dto.response.ExpenseResponse;
import com.hermes.finance.dto.response.IncomeResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CriticalDtoContractTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
}
