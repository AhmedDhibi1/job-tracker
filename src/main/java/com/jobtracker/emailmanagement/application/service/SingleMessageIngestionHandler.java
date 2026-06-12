package com.jobtracker.emailmanagement.application.service;

import com.jobtracker.emailmanagement.application.command.IngestEmailCommand;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.EmailMessageRepository;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.event.EmailIngestedEvent;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.EmailMessage;
import com.jobtracker.emailmanagement.domain.model.RawEmailInput;
import com.jobtracker.emailmanagement.domain.service.EmailParserDomainService;
import com.jobtracker.shared.application.exception.EntityNotFoundException;
import com.jobtracker.shared.application.port.outbound.ApplicationEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SingleMessageIngestionHandler {

    private static final Logger log = LoggerFactory.getLogger(SingleMessageIngestionHandler.class);

    private final EmailAccountRepository accountRepository;
    private final EmailMessageRepository messageRepository;
    private final GmailProviderPort gmailProvider;
    private final EmailParserDomainService emailParser;
    private final ApplicationEventPublisherPort eventPublisher;

    public SingleMessageIngestionHandler(
            EmailAccountRepository accountRepository,
            EmailMessageRepository messageRepository,
            GmailProviderPort gmailProvider,
            EmailParserDomainService emailParser,
            ApplicationEventPublisherPort eventPublisher) {
        this.accountRepository = accountRepository;
        this.messageRepository = messageRepository;
        this.gmailProvider = gmailProvider;
        this.emailParser = emailParser;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmailMessage ingest(IngestEmailCommand command) {
        if (messageRepository.existsByGmailMessageId(command.gmailMessageId())) {
            return messageRepository.findByGmailMessageId(command.gmailMessageId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Message exists but cannot be retrieved: " + command.gmailMessageId()));
        }

        EmailAccount account = accountRepository.findById(command.emailAccountId())
                .orElseThrow(() -> new EntityNotFoundException(
                        EmailAccount.class, command.emailAccountId()));

        if (!account.isActive()) {
            throw new IllegalStateException("Cannot ingest email for deactivated account: " + account.getId());
        }

        RawEmailInput rawMessage = gmailProvider.fetchMessage(account, command.gmailMessageId());

        EmailMessage emailMessage = emailParser.parse(
                rawMessage, account.getId(), account.getEmailAddress());

        messageRepository.save(emailMessage);

        eventPublisher.publish(new EmailIngestedEvent(
                emailMessage.getId(),
                emailMessage.getGmailMessageId(),
                account.getId(),
                resolveCorrelationId(command)));

        return emailMessage;
    }

    private static String resolveCorrelationId(IngestEmailCommand command) {
        if (command.correlationId() != null && !command.correlationId().isBlank()) {
            return command.correlationId();
        }
        return java.util.UUID.randomUUID().toString();
    }
}
