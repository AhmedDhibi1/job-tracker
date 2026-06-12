package com.jobtracker.emailmanagement.application.port.outbound;

import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailAccountRepository {
    EmailAccount save(EmailAccount account);
    Optional<EmailAccount> findById(UUID id);
    Optional<EmailAccount> findByEmailAddress(EmailAddress emailAddress);
    List<EmailAccount> findAll();
    List<EmailAccount> findAllActive();
    boolean existsByEmailAddress(EmailAddress emailAddress);
    boolean existsByIsPrimaryTrue();
}
