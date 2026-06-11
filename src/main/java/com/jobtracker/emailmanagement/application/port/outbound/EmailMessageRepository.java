package com.jobtracker.emailmanagement.application.port.outbound;

import com.jobtracker.emailmanagement.domain.model.EmailMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailMessageRepository {
    EmailMessage save(EmailMessage message);
    Optional<EmailMessage> findById(UUID id);
    boolean existsByGmailMessageId(String gmailMessageId);
    Optional<EmailMessage> findByGmailMessageId(String gmailMessageId);
    List<EmailMessage> findByGmailThreadId(String gmailThreadId);
}