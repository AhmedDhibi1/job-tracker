package com.jobtracker.emailmanagement.application.port.outbound;

import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort.WatchResult;

public interface WatchManagementPort {
    WatchResult setupWatch(EmailAccount account);
    WatchResult renewWatch(EmailAccount account);
}
