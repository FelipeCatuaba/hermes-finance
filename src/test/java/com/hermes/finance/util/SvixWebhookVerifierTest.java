package com.hermes.finance.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SvixWebhookVerifierTest {

    private static final byte[] KEY_BYTES = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
    private static final String SECRET = "whsec_" + Base64.getEncoder().encodeToString(KEY_BYTES);

    private SvixWebhookVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new SvixWebhookVerifier(false);
    }

    @Test
    void shouldRejectWhenSignatureRequiredButSecretMissing() {
        SvixWebhookVerifier strictVerifier = new SvixWebhookVerifier(true);
        assertFalse(strictVerifier.verify(null, "id", "ts", "v1,x", "{}"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldSkipValidationWhenSecretMissing(String secret) {
        assertTrue(verifier.verify(secret, "id", "ts", "v1,invalid", "{}"));
    }

    @Test
    void shouldAcceptValidSignatureWithWhsecPrefix() throws Exception {
        String payload = "{\"type\":\"user.created\"}";
        String svixId = "msg_123";
        String svixTs = "1710000000";
        String signature = "v1," + sign(svixId, svixTs, payload);

        assertTrue(verifier.verify(SECRET, svixId, svixTs, signature, payload));
    }

    @Test
    void shouldAcceptValidSignatureWithoutPrefix() throws Exception {
        String rawSecret = Base64.getEncoder().encodeToString(KEY_BYTES);
        String payload = "{\"type\":\"user.updated\"}";
        String svixId = "msg_456";
        String svixTs = "1710000001";
        String signature = "v1," + signWithKey(rawSecret, svixId, svixTs, payload);

        assertTrue(verifier.verify(rawSecret, svixId, svixTs, signature, payload));
    }

    @Test
    void shouldAcceptWhenOneOfMultipleSignaturesMatches() throws Exception {
        String payload = "{}";
        String svixId = "msg_multi";
        String svixTs = "1710000002";
        String valid = "v1," + sign(svixId, svixTs, payload);
        String header = "v1,invalid " + valid;

        assertTrue(verifier.verify(SECRET, svixId, svixTs, header, payload));
    }

    @Test
    void shouldRejectInvalidSignature() {
        assertFalse(verifier.verify(SECRET, "msg", "ts", "v1,not-valid", "{}"));
    }

    @Test
    void shouldRejectMalformedSecret() {
        assertFalse(verifier.verify("whsec_not-base64!!!", "msg", "ts", "v1,x", "{}"));
    }

    @Test
    void shouldRejectSignatureWithoutVersionPrefix() throws Exception {
        String payload = "{}";
        String svixId = "msg";
        String svixTs = "1";
        String rawSignature = sign(svixId, svixTs, payload);

        assertFalse(verifier.verify(SECRET, svixId, svixTs, rawSignature, payload));
    }

    private String sign(String svixId, String svixTs, String payload) throws Exception {
        return signWithKey(Base64.getEncoder().encodeToString(KEY_BYTES), svixId, svixTs, payload);
    }

    private String signWithKey(String rawSecretBase64, String svixId, String svixTs, String payload) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(rawSecretBase64);
        String message = svixId + "." + svixTs + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }
}
