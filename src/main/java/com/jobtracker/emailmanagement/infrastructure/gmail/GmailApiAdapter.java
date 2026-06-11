package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.GmailHistoryRecord;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.RawGmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Adapter that implements {@link GmailProviderPort} by delegating to the
 * specialized Gmail infrastructure components.
 *
 * <p>This is the single implementation of the {@code GmailProviderPort} outbound
 * port. It acts as a facade over {@link GmailMessageFetcher},
 * {@link GmailHistoryFetcher}, and {@link GmailPushSubscriptionManager}.</p>
 */
@Component
public class GmailApiAdapter implements GmailProviderPort {

    private static final Logger log = LoggerFactory.getLogger(GmailApiAdapter.class);

    private final GmailMessageFetcher messageFetcher;
    private final GmailHistoryFetcher historyFetcher;
    private final GmailPushSubscriptionManager pushManager;

    public GmailApiAdapter(GmailMessageFetcher messageFetcher,
                           GmailHistoryFetcher historyFetcher,
                           GmailPushSubscriptionManager pushManager) {
        this.messageFetcher = messageFetcher;
        this.historyFetcher = historyFetcher;
        this.pushManager = pushManager;
    }

    @Override
    public RawGmailMessage fetchMessage(EmailAccount account, String gmailMessageId) {
        log.debug("Fetching message {} for account {}", gmailMessageId, account.getId());
        return messageFetcher.fetch(account, gmailMessageId);
    }

    @Override
    public List<GmailHistoryRecord> fetchHistoryDelta(EmailAccount account, String fromHistoryId) {
        log.debug("Fetching history delta for account {} from historyId {}", account.getId(), fromHistoryId);
        return historyFetcher.fetchDelta(account, fromHistoryId);
    }

    @Override
    public List<String> listMessageIdsSince(EmailAccount account, Instant afterDate) {
        log.debug("Listing message IDs for account {} since {}", account.getId(), afterDate);
        return messageFetcher.listMessageIdsSince(account, afterDate);
    }

    @Override
    public WatchResult setupWatch(EmailAccount account) {
        log.info("Setting up Gmail watch for account {}", account.getId());
        return pushManager.setupWatch(account);
    }

    @Override
    public WatchResult renewWatch(EmailAccount account) {
        log.info("Renewing Gmail watch for account {}", account.getId());
        return pushManager.renewWatch(account);
    }
}