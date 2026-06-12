package com.jobtracker.emailmanagement.infrastructure.gmail.pubsub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class PubSubPayloadDecoder {

    private static final Logger log = LoggerFactory.getLogger(PubSubPayloadDecoder.class);

    private final ObjectMapper objectMapper;

    public PubSubPayloadDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PubSubNotification decode(String base64Payload) {
        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(base64Payload);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Base64 Pub/Sub payload", e);
            throw new IllegalArgumentException("Invalid Base64 encoding in Pub/Sub payload", e);
        }

        String jsonPayload = new String(decodedBytes, StandardCharsets.UTF_8);
        log.debug("Decoded Pub/Sub payload: {}", jsonPayload);

        try {
            JsonNode root = objectMapper.readTree(jsonPayload);

            String emailAddress = extractField(root, "emailAddress");
            String historyId = extractField(root, "historyId");

            if (emailAddress == null || emailAddress.isBlank()) {
                throw new IllegalArgumentException("Pub/Sub payload missing 'emailAddress' field");
            }
            if (historyId == null || historyId.isBlank()) {
                throw new IllegalArgumentException("Pub/Sub payload missing 'historyId' field");
            }

            return new PubSubNotification(emailAddress, historyId, null);

        } catch (IOException e) {
            log.error("Failed to parse Pub/Sub payload", e);
            throw new IllegalArgumentException("Invalid Pub/Sub payload format", e);
        }
    }

    public PubSubNotification decodeEnvelope(String envelopeJson) {
        try {
            JsonNode root = objectMapper.readTree(envelopeJson);
            JsonNode message = root.path("message");

            if (message.isMissingNode()) {
                throw new IllegalArgumentException("Pub/Sub envelope missing 'message' field");
            }

            String accountId = message.path("attributes").path("accountId").asText(null);
            String base64Data = message.path("data").asText();
            if (base64Data == null || base64Data.isEmpty()) {
                throw new IllegalArgumentException("Pub/Sub message missing 'data' field");
            }

            PubSubNotification notification = decode(base64Data);
            String resolvedAccountId = (accountId != null && !accountId.isBlank())
                    ? accountId
                    : notification.accountId();
            if (resolvedAccountId == null) {
                throw new IllegalArgumentException(
                        "Pub/Sub notification missing accountId and cannot be resolved from accountEmail: "
                        + notification.accountEmail());
            }
            return new PubSubNotification(
                    notification.accountEmail(),
                    notification.historyId(),
                    resolvedAccountId);

        } catch (IOException e) {
            log.error("Failed to parse Pub/Sub envelope", e);
            throw new IllegalArgumentException("Invalid Pub/Sub envelope format", e);
        }
    }

    private String extractField(JsonNode root, String fieldName) {
        JsonNode node = root.path(fieldName);
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    public record PubSubNotification(
            String accountEmail,
            String historyId,
            String accountId
    ) {
        public PubSubNotification {
            if (accountEmail == null || accountEmail.isBlank()) {
                throw new IllegalArgumentException("accountEmail must not be blank");
            }
            if (historyId == null || historyId.isBlank()) {
                throw new IllegalArgumentException("historyId must not be blank");
            }
        }

        public PubSubNotification(String accountEmail, String historyId) {
            this(accountEmail, historyId, null);
        }
    }
}
