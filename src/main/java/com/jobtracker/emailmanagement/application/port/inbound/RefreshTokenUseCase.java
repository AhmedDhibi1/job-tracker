package com.jobtracker.emailmanagement.application.port.inbound;

import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import java.util.UUID;

public interface RefreshTokenUseCase {
    EmailAccount refreshAndPersist(UUID accountId);
}
