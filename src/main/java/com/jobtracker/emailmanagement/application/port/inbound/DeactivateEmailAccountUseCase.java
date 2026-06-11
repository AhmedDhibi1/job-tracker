package com.jobtracker.emailmanagement.application.port.inbound;
import java.util.UUID;

public interface DeactivateEmailAccountUseCase {
    void deactivate(UUID accountId, String reason);
}
