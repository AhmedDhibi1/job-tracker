package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.RawGmailMessage;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
public class GmailMessageFetcher {

    private static final Logger log = LoggerFactory.getLogger(GmailMessageFetcher.class);

    private static final String HEADER_FROM = "from";
    private static final String HEADER_TO = "to";
    private static final String HEADER_SUBJECT = "subject";
    private static final String HEADER_DATE = "date";

    private static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    private static final String MIME_TYPE_TEXT_HTML = "text/html";
    private static final String MIME_TYPE_MULTIPART = "multipart/";

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss ZZ", Locale.ENGLISH),
        DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH),
    };

    private final GmailClientFactory clientFactory;

    public GmailMessageFetcher(GmailClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }


    public RawGmailMessage fetch(EmailAccount account, String gmailMessageId) {
        Gmail client = clientFactory.getClient(account);

        try {
            Message message = client.users().messages()
                    .get("me", gmailMessageId)
                    .setFormat("full")
                    .execute();

            return parseMessage(message, account);

        } catch (IOException e) {
            log.error("Failed to fetch message {} for account {}: {}",
                    gmailMessageId, account.getId(), e.getMessage());
            throw new RuntimeException("Gmail message fetch failed: " + e.getMessage(), e);
        }
    }

    public List<String> listMessageIdsSince(EmailAccount account, Instant afterDate) {
        Gmail client = clientFactory.getClient(account);

        try {
            List<String> messageIds = new ArrayList<>();
            String pageToken = null;
            long afterMillis = afterDate.toEpochMilli();

            do {
                Gmail.Users.Messages.List request = client.users().messages()
                        .list("me")
                        .setQ("after:" + (afterMillis / 1000))
                        .setMaxResults(500L);

                if (pageToken != null) {
                    request.setPageToken(pageToken);
                }

                var response = request.execute();
                pageToken = response.getNextPageToken();

                if (response.getMessages() != null) {
                    for (var m : response.getMessages()) {
                        messageIds.add(m.getId());
                    }
                }
            } while (pageToken != null);

            log.info("Listed {} message IDs for account {} since {}",
                    messageIds.size(), account.getId(), afterDate);
            return messageIds;

        } catch (IOException e) {
            log.error("Failed to list messages for account {}: {}", account.getId(), e.getMessage());
            throw new RuntimeException("Gmail message list failed: " + e.getMessage(), e);
        }
    }


    private RawGmailMessage parseMessage(Message message, EmailAccount account) {
        Map<String, String> headers = extractHeaders(message.getPayload());

        String from = headers.getOrDefault(HEADER_FROM, "");
        List<String> to = parseAddressList(headers.getOrDefault(HEADER_TO, ""));
        String subject = headers.getOrDefault(HEADER_SUBJECT, "");
        Instant sentAt = parseDateHeader(message, headers.get(HEADER_DATE));

        List<MessagePart> allParts = flattenParts(message.getPayload());

        String bodyText = extractBody(allParts, MIME_TYPE_TEXT_PLAIN);
        String bodyHtml = extractBody(allParts, MIME_TYPE_TEXT_HTML);
        List<RawGmailMessage.MimePart> mimeParts = allParts.stream().map(this::toRawMimePart).toList();

        List<String> accountEmails = List.of(account.getEmailAddress().value());

        return new RawGmailMessage(
                message.getId(),
                message.getThreadId(),
                headers,
                mimeParts,
                sentAt,
                from,
                to,
                subject,
                bodyText,
                bodyHtml,
                accountEmails
        );
    }

    private RawGmailMessage.MimePart toRawMimePart(MessagePart part) {
        String filename = part.getFilename() != null ? part.getFilename() : "";
        String attachmentId = part.getBody() != null ? part.getBody().getAttachmentId() : null;
        long size = part.getBody() != null && part.getBody().getSize() != null
                ? part.getBody().getSize() : 0L;
        String body = null;

        if (attachmentId == null && part.getBody() != null && part.getBody().getData() != null) {
            byte[] decoded = Base64.decodeBase64(part.getBody().getData());
            body = new String(decoded, StandardCharsets.UTF_8);
        }

        String contentDisposition = part.getHeaders() != null
                ? part.getHeaders().stream()
                        .filter(h -> "content-disposition".equalsIgnoreCase(h.getName()))
                        .findFirst()
                        .map(MessagePartHeader::getValue)
                        .orElse(null)
                : null;

        return new RawGmailMessage.MimePart(
                part.getMimeType(),
                contentDisposition,
                filename,
                body,
                attachmentId,
                size
        );
    }

    private Map<String, String> extractHeaders(MessagePart payload) {
        Map<String, String> headers = new HashMap<>();
        if (payload == null || payload.getHeaders() == null) {
            return headers;
        }
        for (MessagePartHeader header : payload.getHeaders()) {
            headers.put(header.getName().toLowerCase(), header.getValue());
        }
        return headers;
    }

    private List<String> parseAddressList(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return List.of();
        }
        try {
            return Arrays.stream(InternetAddress.parse(headerValue))
                    .map(InternetAddress::getAddress)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (AddressException e) {
            log.warn("Failed to parse address list '{}', falling back to split", headerValue);
            return Arrays.stream(headerValue.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    private Instant parseDateHeader(Message message, String dateHeader) {
        if (dateHeader != null) {
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    return ZonedDateTime.parse(dateHeader.trim(), formatter).toInstant();
                } catch (DateTimeParseException e) {
                    // Try next formatter
                }
            }
            log.warn("All date formatters failed for '{}', falling back to internalDate", dateHeader);
        }
        if (message.getInternalDate() != null) {
            log.debug("Using internalDate fallback for message {}", message.getId());
            return Instant.ofEpochMilli(message.getInternalDate());
        }
        log.warn("No date header or internalDate available for message {}, using Instant.EPOCH", message.getId());
        return Instant.EPOCH;
    }

    private List<MessagePart> flattenParts(MessagePart root) {
        List<MessagePart> result = new ArrayList<>();
        if (root == null) {
            return result;
        }

        String mimeType = root.getMimeType();
        if (mimeType != null && mimeType.toLowerCase().startsWith(MIME_TYPE_MULTIPART)) {
            if (root.getParts() != null) {
                for (MessagePart child : root.getParts()) {
                    result.addAll(flattenParts(child));
                }
            }
        } else {
            result.add(root);
        }
        return result;
    }

    private String extractBody(List<MessagePart> parts, String targetMimeType) {
        for (MessagePart part : parts) {
            if (targetMimeType.equalsIgnoreCase(part.getMimeType())) {
                MessagePartBody body = part.getBody();
                if (body != null && body.getData() != null) {
                    byte[] decoded = Base64.decodeBase64(body.getData());
                    return new String(decoded, StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }
}
