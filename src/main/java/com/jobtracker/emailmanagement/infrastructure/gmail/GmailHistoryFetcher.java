package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.GmailHistoryRecord;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.HistoryDeltaInfraResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Component
public class GmailHistoryFetcher {

    private static final Logger log = LoggerFactory.getLogger(GmailHistoryFetcher.class);

    private static final long PAGE_SIZE = 100;

    private final GmailClientFactory clientFactory;

    public GmailHistoryFetcher(GmailClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public HistoryDeltaInfraResult fetchDelta(EmailAccount account, String fromHistoryId) {
        Gmail client = clientFactory.getClient(account);
        List<GmailHistoryRecord> records = new ArrayList<>();

        if (fromHistoryId == null || fromHistoryId.isBlank()) {
            log.warn("No historyId provided for account {}. Cannot perform delta sync.", account.getId());
            return new HistoryDeltaInfraResult(List.of(), null);
        }

        BigInteger startHistoryId;
        try {
            startHistoryId = new BigInteger(fromHistoryId.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid historyId format: " + fromHistoryId, e);
        }

        String latestHistoryId = fromHistoryId;
        String pageToken = null;

        try {
            do {
                Gmail.Users.History.List request = client.users().history()
                        .list("me")
                        .setStartHistoryId(startHistoryId)
                        .setMaxResults(PAGE_SIZE);

                if (pageToken != null) {
                    request.setPageToken(pageToken);
                }

                ListHistoryResponse response = request.execute();
                pageToken = response.getNextPageToken();

                if (response.getHistory() != null) {
                    for (History history : response.getHistory()) {
                        List<String> addedMessageIds = extractAddedMessageIds(history);
                        if (!addedMessageIds.isEmpty()) {
                            records.add(new GmailHistoryRecord(
                                    String.valueOf(history.getId()),
                                    addedMessageIds
                            ));
                        }
                    }
                }

                if (response.getHistoryId() != null) {
                    latestHistoryId = String.valueOf(response.getHistoryId());
                }

            } while (pageToken != null);

            log.info("Fetched {} history records for account {} since historyId {}",
                    records.size(), account.getId(), fromHistoryId);

            return new HistoryDeltaInfraResult(records, latestHistoryId);

        } catch (IOException e) {
            log.error("Failed to fetch history delta for account {}", account.getId(), e);
            throw new GmailApiException("Gmail history fetch failed", e,
                    account.getId().toString(), GmailApiException.Operation.FETCH_HISTORY);
        }
    }

    private List<String> extractAddedMessageIds(History history) {
        List<String> ids = new ArrayList<>();
        if (history.getMessagesAdded() != null) {
            for (HistoryMessageAdded added : history.getMessagesAdded()) {
                if (added.getMessage() != null && added.getMessage().getId() != null) {
                    ids.add(added.getMessage().getId());
                }
            }
        }
        return ids;
    }
}
