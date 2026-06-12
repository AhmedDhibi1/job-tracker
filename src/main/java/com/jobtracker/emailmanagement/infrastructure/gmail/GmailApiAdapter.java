package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.jobtracker.emailmanagement.application.dto.EmailHistoryDelta;
import com.jobtracker.emailmanagement.application.dto.FetchedEmailData;
import com.jobtracker.emailmanagement.application.dto.HistoryDeltaResult;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.RawGmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

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
    public FetchedEmailData fetchMessage(EmailAccount account, String gmailMessageId) {
        log.debug("Fetching message {} for account {}", gmailMessageId, account.getId());
        RawGmailMessage raw = messageFetcher.fetch(account, gmailMessageId);
        return toFetchedEmailData(raw);
    }

    @Override
    public HistoryDeltaResult fetchHistoryDelta(EmailAccount account, String fromHistoryId) {
        log.debug("Fetching history delta for account {} from historyId {}", account.getId(), fromHistoryId);
        var infraResult = historyFetcher.fetchDelta(account, fromHistoryId);
        List<EmailHistoryDelta> records = infraResult.records().stream()
                .map(r -> new EmailHistoryDelta(r.newHistoryId(), r.addedMessageIds()))
                .toList();
        return new HistoryDeltaResult(records, infraResult.latestHistoryId());
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

    private static FetchedEmailData toFetchedEmailData(RawGmailMessage raw) {
        List<FetchedEmailData.AttachmentPart> parts = raw.parts() == null ? List.of()
                : raw.parts().stream()
                    .map(p -> new FetchedEmailData.AttachmentPart(
                            p.mimeType(), p.contentDisposition(), p.filename(),
                            p.body(), p.gmailAttachmentId(), p.sizeBytes()))
                    .toList();

        return new FetchedEmailData(
                raw.gmailMessageId(),
                raw.gmailThreadId(),
                raw.headers(),
                parts,
                raw.sentAt(),
                raw.from(),
                raw.to(),
                raw.subject(),
                raw.bodyText(),
                raw.bodyHtml(),
                raw.accountEmailAddresses()
        );
    }
}
