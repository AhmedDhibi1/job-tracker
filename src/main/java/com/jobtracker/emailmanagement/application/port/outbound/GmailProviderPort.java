package com.jobtracker.emailmanagement.application.port.outbound;

import com.jobtracker.emailmanagement.application.dto.HistoryDeltaResult;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.RawEmailInput;
import java.time.Instant;
import java.util.List;

public interface GmailProviderPort {

    RawEmailInput fetchMessage(EmailAccount account, String gmailMessageId);

    HistoryDeltaResult fetchHistoryDelta(EmailAccount account, String fromHistoryId);

    List<String> listMessageIdsSince(EmailAccount account, Instant afterDate);

    WatchResult setupWatch(EmailAccount account);

    WatchResult renewWatch(EmailAccount account);

    record WatchResult(Instant expiration, String historyId) {}
}
