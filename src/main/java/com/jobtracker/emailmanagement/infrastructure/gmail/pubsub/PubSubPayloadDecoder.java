package com.jobtracker.emailmanagement.infrastructure.gmail.pubsub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Decodes Base64-encoded Pub/Sub push payloads from Gmail watch notifications.
 *
 * <p>Google Cloud Pub/Sub delivers push messages with the following structure:</p>
 * <pre>{@code
 * {
 *   "message": {
 *     "data": "<base64-encoded-payload>",
 *     "attributes": {
 *       "accountId": "..."
 *     },
 *     "messageId": "...",
 *     "publishTime": "..."
 *   },
 *   "subscription": "projects/.../subscriptions/..."
 * }
 * }</pre>
 *
 * <p>The inner {@code data} field is a Base64-encoded JSON object containing:
 * <pre>{@code
 * {
 *   "emailAddress": "user@gmail.com",
 *   "historyId": "123456789"
 * }
 * }</pre>
 */
@Component
public class PubSubPayloadDecoder {

    private static final Logger log = LoggerFactory.getLogger(PubSubPayloadDecoder.class);

    private final ObjectMapper objectMapper;

    public PubSubPayloadDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Decodes a Pub/Sub push notification payload.
     *
     * @param base64Payload the raw Base64-encoded Pub/Sub message data
     *                      (the {@code message.data} field from the HTTP body)
     * @return a {@link PubSubNotification} containing the extracted email and historyId
     * @throws IllegalArgumentException if the payload cannot be decoded or parsed
     */
    public PubSubNotification decode(String base64Payload) {
        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(base64Payload);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Base64 Pub/Sub payload: {}", e.getMessage());
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

            return new PubSubNotification(emailAddress, historyId);

        } catch (IOException e) {
            log.error("Failed to parse Pub/Sub payload: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid Pub/Sub payload format", e);
        }
    }

    /**
     * Decodes the full Pub/Sub envelope (not just the inner data).
     * Use this when receiving the complete HTTP body from Google.
     *
     * @param envelopeJson the complete Pub/Sub push envelope as JSON string
     * @return a {@link PubSubNotification}
     */
    public PubSubNotification decodeEnvelope(String envelopeJson) {
        try {
            JsonNode root = objectMapper.readTree(envelopeJson);
            JsonNode message = root.path("message");

            if (message.isMissingNode()) {
                throw new IllegalArgumentException("Pub/Sub envelope missing 'message' field");
            }

            String base64Data = message.path("data").asText();
            if (base64Data == null || base64Data.isEmpty()) {
                throw new IllegalArgumentException("Pub/Sub message missing 'data' field");
            }

            return decode(base64Data);

        } catch (IOException e) {
            log.error("Failed to parse Pub/Sub envelope: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid Pub/Sub envelope format", e);
        }
    }

    private String extractField(JsonNode root, String fieldName) {
        JsonNode node = root.path(fieldName);
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    /**
     * Immutable record representing the decoded Gmail notification.
     */
    public record PubSubNotification(
            String accountEmail,
            String historyId
    ) {
        public PubSubNotification {
            if (accountEmail == null || accountEmail.isBlank()) {
                throw new IllegalArgumentException("accountEmail must not be blank");
            }
            if (historyId == null || historyId.isBlank()) {
                throw new IllegalArgumentException("historyId must not be blank");
            }
        }
    }
}