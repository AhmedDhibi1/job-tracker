package com.jobtracker.emailmanagement.application.service;

import com.jobtracker.emailmanagement.application.command.IngestEmailCommand;
import com.jobtracker.emailmanagement.application.command.InitialSyncCommand;
import com.jobtracker.emailmanagement.application.port.inbound.IngestEmailUseCase;
import com.jobtracker.emailmanagement.application.port.inbound.InitialSyncUseCase;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.EmailMessageRepository;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.event.EmailIngestedEvent;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.EmailMessage;
import com.jobtracker.emailmanagement.domain.service.EmailParserDomainService;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.GmailHistoryRecord;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.RawGmailMessage;
import com.jobtracker.shared.application.exception.EntityNotFoundException;
import com.jobtracker.shared.application.port.outbound.ApplicationEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmailIngestionApplicationService
        implements IngestEmailUseCase, InitialSyncUseCase {

    private static final Logger log = LoggerFactory.getLogger(EmailIngestionApplicationService.class);

    private final EmailAccountRepository       accountRepository;
    private final EmailMessageRepository       messageRepository;
    private final GmailProviderPort            gmailProvider;
    private final EmailParserDomainService     emailParser;
    private final ApplicationEventPublisherPort eventPublisher;

    public EmailIngestionApplicationService(
            EmailAccountRepository accountRepository,
            EmailMessageRepository messageRepository,
            GmailProviderPort gmailProvider,
            EmailParserDomainService emailParser,
            ApplicationEventPublisherPort eventPublisher) {
        this.accountRepository = accountRepository;
        this.messageRepository = messageRepository;
        this.gmailProvider     = gmailProvider;
        this.emailParser       = emailParser;
        this.eventPublisher    = eventPublisher;
    }

    @Override
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

        RawGmailMessage rawMessage = gmailProvider.fetchMessage(account, command.gmailMessageId());

        EmailMessage emailMessage = emailParser.parse(
                rawMessage, account.getId(), account.getEmailAddress());

        messageRepository.save(emailMessage);

        eventPublisher.publish(new EmailIngestedEvent(
                emailMessage.getId(),
                emailMessage.getGmailMessageId(),
                account.getId(),
                correlationId()));

        return emailMessage;
    }

    @Override
    @Transactional
    public int sync(InitialSyncCommand command) {
        EmailAccount account = accountRepository.findById(command.emailAccountId())
                .orElseThrow(() -> new EntityNotFoundException(
                        EmailAccount.class, command.emailAccountId()));

        List<String> messageIds;
        String latestHistoryId = null;

        if (account.getSyncState().historyId() == null) {
            Instant afterDate = Instant.now().minus(command.daysBack(), ChronoUnit.DAYS);
            messageIds = gmailProvider.listMessageIdsSince(account, afterDate);
        } else {
            List<GmailHistoryRecord> records = gmailProvider.fetchHistoryDelta(
                    account, account.getSyncState().historyId());
            messageIds = records.stream()
                    .flatMap(r -> r.addedMessageIds().stream())
                    .distinct()
                    .toList();
            if (!records.isEmpty()) {
                latestHistoryId = records.getLast().newHistoryId();
            }
        }

        List<String> failedIds = new ArrayList<>();
        int count = 0;
        for (String gmailMessageId : messageIds) {
            try {
                ingest(new IngestEmailCommand(
                        command.emailAccountId(), gmailMessageId, correlationId()));
                count++;
            } catch (Exception e) {
                log.error("Failed to ingest message {} for account {}: {}",
                        gmailMessageId, command.emailAccountId(), e.getMessage(), e);
                failedIds.add(gmailMessageId);
            }
        }

        if (!failedIds.isEmpty()) {
            log.warn("{} messages failed during sync for account {}",
                    failedIds.size(), command.emailAccountId());
        }

        if (latestHistoryId != null) {
            account.updateSyncState(account.getSyncState().withUpdatedHistoryId(latestHistoryId));
            accountRepository.save(account);
        }

        return count;
    }

    private static String correlationId() {
        String mdc = MDC.get("correlationId");
        return mdc != null ? mdc : UUID.randomUUID().toString();
    }
}