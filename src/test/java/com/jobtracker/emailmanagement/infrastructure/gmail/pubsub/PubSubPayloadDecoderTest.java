package com.jobtracker.emailmanagement.infrastructure.gmail.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PubSubPayloadDecoderTest {

    private PubSubPayloadDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new PubSubPayloadDecoder(new ObjectMapper());
    }

    @Test
    void decode_validPayload() {
        String json = "{\"emailAddress\":\"user@example.com\",\"historyId\":\"12345\"}";
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        PubSubPayloadDecoder.PubSubNotification result = decoder.decode(base64);

        assertThat(result.accountEmail()).isEqualTo("user@example.com");
        assertThat(result.historyId()).isEqualTo("12345");
        assertThat(result.accountId()).isNull();
    }

    @Test
    void decode_invalidBase64_throws() {
        assertThatThrownBy(() -> decoder.decode("not-valid-base64!!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    void decode_invalidJson_throws() {
        String base64 = Base64.getEncoder().encodeToString("not json".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> decoder.decode(base64))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("payload format");
    }

    @Test
    void decode_missingEmailAddress_throws() {
        String json = "{\"historyId\":\"12345\"}";
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> decoder.decode(base64))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("emailAddress");
    }

    @Test
    void decode_blankEmailAddress_throws() {
        String json = "{\"emailAddress\":\"\",\"historyId\":\"12345\"}";
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> decoder.decode(base64))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("emailAddress");
    }

    @Test
    void decode_missingHistoryId_throws() {
        String json = "{\"emailAddress\":\"user@example.com\"}";
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> decoder.decode(base64))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("historyId");
    }

    @Test
    void decodeEnvelope_validPayload() {
        String innerJson = "{\"emailAddress\":\"user@example.com\",\"historyId\":\"12345\"}";
        String innerBase64 = Base64.getEncoder().encodeToString(innerJson.getBytes(StandardCharsets.UTF_8));
        String envelope = "{\"message\":{\"attributes\":{\"accountId\":\"acc-123\"},\"data\":\"" + innerBase64 + "\"}}";

        PubSubPayloadDecoder.PubSubNotification result = decoder.decodeEnvelope(envelope);

        assertThat(result.accountEmail()).isEqualTo("user@example.com");
        assertThat(result.historyId()).isEqualTo("12345");
        assertThat(result.accountId()).isEqualTo("acc-123");
    }

    @Test
    void decodeEnvelope_usesAccountEmail_whenNoAccountIdInAttributes() {
        String innerJson = "{\"emailAddress\":\"user@example.com\",\"historyId\":\"12345\",\"accountId\":\"acc-999\"}";
        String innerBase64 = Base64.getEncoder().encodeToString(innerJson.getBytes(StandardCharsets.UTF_8));
        String envelope = "{\"message\":{\"attributes\":{},\"data\":\"" + innerBase64 + "\"}}";

        PubSubPayloadDecoder.PubSubNotification result = decoder.decodeEnvelope(envelope);

        assertThat(result.accountId()).isEqualTo("acc-999");
    }

    @Test
    void decodeEnvelope_missingMessage_throws() {
        assertThatThrownBy(() -> decoder.decodeEnvelope("{}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("message");
    }

    @Test
    void decodeEnvelope_missingData_throws() {
        String envelope = "{\"message\":{\"attributes\":{}}}";
        assertThatThrownBy(() -> decoder.decodeEnvelope(envelope))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data");
    }

    @Test
    void decodeEnvelope_emptyData_throws() {
        String envelope = "{\"message\":{\"attributes\":{},\"data\":\"\"}}";
        assertThatThrownBy(() -> decoder.decodeEnvelope(envelope))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data");
    }

    @Test
    void decodeEnvelope_invalidJson_throws() {
        assertThatThrownBy(() -> decoder.decodeEnvelope("not json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("envelope format");
    }

    @Test
    void decodeEnvelope_resolvesAccountIdFromEmail_whenAttributeMissing() {
        String innerJson = "{\"emailAddress\":\"user@example.com\",\"historyId\":\"12345\",\"accountId\":\"acc-456\"}";
        String innerBase64 = Base64.getEncoder().encodeToString(innerJson.getBytes(StandardCharsets.UTF_8));
        String envelope = "{\"message\":{\"data\":\"" + innerBase64 + "\"}}";

        PubSubPayloadDecoder.PubSubNotification result = decoder.decodeEnvelope(envelope);

        assertThat(result.accountEmail()).isEqualTo("user@example.com");
        assertThat(result.historyId()).isEqualTo("12345");
        assertThat(result.accountId()).isEqualTo("acc-456");
    }

    @Test
    void pubSubNotification_rejectsNullAccountEmail() {
        assertThatThrownBy(() -> new PubSubPayloadDecoder.PubSubNotification(null, "h1", "a1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("accountEmail");
    }

    @Test
    void pubSubNotification_rejectsBlankAccountEmail() {
        assertThatThrownBy(() -> new PubSubPayloadDecoder.PubSubNotification("", "h1", "a1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("accountEmail");
    }

    @Test
    void pubSubNotification_rejectsNullHistoryId() {
        assertThatThrownBy(() -> new PubSubPayloadDecoder.PubSubNotification("a@b.com", null, "a1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("historyId");
    }

    @Test
    void pubSubNotification_rejectsBlankHistoryId() {
        assertThatThrownBy(() -> new PubSubPayloadDecoder.PubSubNotification("a@b.com", "", "a1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("historyId");
    }

    @Test
    void pubSubNotification_twoArgConstructor() {
        PubSubPayloadDecoder.PubSubNotification n = new PubSubPayloadDecoder.PubSubNotification("a@b.com", "h1");
        assertThat(n.accountEmail()).isEqualTo("a@b.com");
        assertThat(n.historyId()).isEqualTo("h1");
        assertThat(n.accountId()).isNull();
    }

    @Test
    void decodeEnvelope_throws_whenBothEmptyAccountIds() {
        String innerJson = "{\"emailAddress\":\"user@example.com\",\"historyId\":\"12345\"}";
        String innerBase64 = Base64.getEncoder().encodeToString(innerJson.getBytes(StandardCharsets.UTF_8));
        String envelope = "{\"message\":{\"data\":\"" + innerBase64 + "\"}}";

        assertThatThrownBy(() -> decoder.decodeEnvelope(envelope))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing accountId");
    }
}
