package com.jobtracker.emailmanagement.application.port.outbound;

import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.GmailHistoryRecord;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.RawGmailMessage;
import java.time.Instant;
import java.util.List;


public interface GmailProviderPort {

    RawGmailMessage fetchMessage(EmailAccount account, String gmailMessageId);

    List<GmailHistoryRecord> fetchHistoryDelta(EmailAccount account, String fromHistoryId);

    List<String> listMessageIdsSince(EmailAccount account, Instant afterDate);

    WatchResult setupWatch(EmailAccount account);

    WatchResult renewWatch(EmailAccount account);

    record WatchResult(Instant expiration, String historyId) {}
}