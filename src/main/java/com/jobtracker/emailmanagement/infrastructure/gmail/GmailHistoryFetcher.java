package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.GmailHistoryRecord;
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

    /** Maximum number of history records per page. */
    private static final long PAGE_SIZE = 100;

    private final GmailClientFactory clientFactory;

    public GmailHistoryFetcher(GmailClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public List<GmailHistoryRecord> fetchDelta(EmailAccount account, String fromHistoryId) {
        Gmail client = clientFactory.getClient(account);
        List<GmailHistoryRecord> records = new ArrayList<>();

        if (fromHistoryId == null || fromHistoryId.isBlank()) {
            log.warn("No historyId provided for account {}. Cannot perform delta sync.", account.getId());
            return records;
        }

        long startHistoryId;
        try {
            startHistoryId = Long.parseLong(fromHistoryId.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid historyId format: " + fromHistoryId, e);
        }

        BigInteger originalStartHistoryId = BigInteger.valueOf(startHistoryId);
        String latestHistoryId = fromHistoryId;
        String pageToken = null;

        try {
            do {
                Gmail.Users.History.List request = client.users().history()
                        .list("me")
                        .setStartHistoryId(originalStartHistoryId)
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

            if (!records.isEmpty()) {
                records.set(records.size() - 1,
                        new GmailHistoryRecord(latestHistoryId, records.getLast().addedMessageIds()));
            }

            return records;

        } catch (IOException e) {
            log.error("Failed to fetch history delta for account {}: {}", account.getId(), e.getMessage());
            throw new RuntimeException("Gmail history fetch failed: " + e.getMessage(), e);
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
