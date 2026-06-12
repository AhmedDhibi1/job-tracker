package com.jobtracker.emailmanagement.application.port.outbound;

import com.jobtracker.emailmanagement.domain.model.EmailAccount;

public interface PushSubscriptionPort {
    void stopWatch(EmailAccount account);
}
