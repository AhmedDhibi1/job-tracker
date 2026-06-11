package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort.WatchResult;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class GmailPushSubscriptionManager {

    private static final Logger log = LoggerFactory.getLogger(GmailPushSubscriptionManager.class);

    private final GmailClientFactory clientFactory;
    private final String pubSubTopicName;
    private final List<String> watchLabelIds;

    public GmailPushSubscriptionManager(
            GmailClientFactory clientFactory,
            @Value("${gmail.pubsub.topic-name}") String pubSubTopicName,
            @Value("${gmail.watch.label-ids:INBOX}") List<String> watchLabelIds) {
        this.clientFactory = clientFactory;
        this.pubSubTopicName = pubSubTopicName;
        this.watchLabelIds = watchLabelIds;
    }

    public WatchResult setupWatch(EmailAccount account) {
        if (pubSubTopicName == null || pubSubTopicName.isBlank()) {
            throw new IllegalStateException(
                    "gmail.pubsub.topic-name is not configured. Push notifications cannot be enabled.");
        }

        Gmail client = clientFactory.getClient(account);

        try {
            WatchRequest request = new WatchRequest()
                    .setTopicName(pubSubTopicName)
                    .setLabelIds(watchLabelIds)
                    .setLabelFilterAction("include");

            WatchResponse response = client.users().watch("me", request).execute();

            Instant expiration = Instant.ofEpochMilli(response.getExpiration());
            String historyId = response.getHistoryId() != null
                    ? String.valueOf(response.getHistoryId())
                    : null;

            log.info("Gmail watch established for account {}. Expires at {} (historyId: {})",
                    account.getId(), expiration, historyId);

            return new WatchResult(expiration, historyId);

        } catch (IOException e) {
            log.error("Failed to setup Gmail watch for account {}: {}", account.getId(), e.getMessage());
            throw new RuntimeException("Gmail watch setup failed: " + e.getMessage(), e);
        }
    }

    public WatchResult renewWatch(EmailAccount account) {
        log.debug("Renewing Gmail watch for account {}", account.getId());
        return setupWatch(account);
    }

    public void stopWatch(EmailAccount account) {
        Gmail client = clientFactory.getClient(account);

        try {
            client.users().stop("me").execute();
            log.info("Gmail watch stopped for account {}", account.getId());
        } catch (IOException e) {
            log.warn("Failed to stop Gmail watch for account {}: {}. " +
                    "The watch will expire naturally.", account.getId(), e.getMessage());
        }
    }
}
