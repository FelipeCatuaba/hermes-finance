package com.hermes.finance.util;

import com.hermes.finance.domain.webhook.WebhookSignatureVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Verifica assinaturas de webhooks do Clerk via protocolo Svix (HMAC-SHA256).
 */
@Component
public class SvixWebhookVerifier implements WebhookSignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(SvixWebhookVerifier.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SECRET_PREFIX = "whsec_";

    private final boolean requireSignature;

    public SvixWebhookVerifier(
            @Value("${clerk.webhook.require-signature:false}") boolean requireSignature) {
        this.requireSignature = requireSignature;
    }

    /**
     * @param secret  valor de CLERK_WEBHOOK_SECRET (ex: "whsec_...")
     * @param svixId  header "svix-id"
     * @param svixTs  header "svix-timestamp"
     * @param svixSig header "svix-signature" (ex: "v1,BASE64SIGNATURE")
     * @param payload corpo raw da requisição
     */
    public boolean verify(String secret, String svixId, String svixTs, String svixSig, String payload) {
        if (secret == null || secret.isBlank()) {
            if (requireSignature) {
                log.warn("Webhook rejeitado: CLERK_WEBHOOK_SECRET obrigatório mas não configurado");
                return false;
            }
            log.warn("Validação Svix desativada (CLERK_WEBHOOK_SECRET ausente — apenas desenvolvimento)");
            return true;
        }

        try {
            String rawSecret = secret.startsWith(SECRET_PREFIX)
                ? secret.substring(SECRET_PREFIX.length())
                : secret;
            byte[] keyBytes = Base64.getDecoder().decode(rawSecret);

            String message = svixId + "." + svixTs + "." + payload;
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(keyBytes, HMAC_ALGORITHM));
            String computed = Base64.getEncoder().encodeToString(
                mac.doFinal(message.getBytes(StandardCharsets.UTF_8))
            );

            for (String part : svixSig.split(" ")) {
                if (part.startsWith("v1,")) {
                    String candidate = part.substring(3);
                    if (computed.equals(candidate)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.debug("Falha ao verificar assinatura Svix", e);
            return false;
        }
    }
}
