package com.jobtracker.emailmanagement.application.service;

import com.jobtracker.emailmanagement.application.command.IngestEmailCommand;
import com.jobtracker.emailmanagement.application.command.InitialSyncCommand;
import com.jobtracker.emailmanagement.application.dto.HistoryDeltaResult;
import com.jobtracker.emailmanagement.application.port.inbound.IngestEmailUseCase;
import com.jobtracker.emailmanagement.application.port.inbound.InitialSyncUseCase;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.EmailMessage;
import com.jobtracker.shared.CorrelationIdHolder;
import com.jobtracker.shared.application.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailIngestionApplicationService
        implements IngestEmailUseCase, InitialSyncUseCase {

    private static final Logger log = LoggerFactory.getLogger(EmailIngestionApplicationService.class);

    private final EmailAccountRepository       accountRepository;
    private final GmailProviderPort            gmailProvider;
    private final SingleMessageIngestionHandler ingestionHandler;

    public EmailIngestionApplicationService(
            EmailAccountRepository accountRepository,
            GmailProviderPort gmailProvider,
            SingleMessageIngestionHandler ingestionHandler) {
        this.accountRepository = accountRepository;
        this.gmailProvider     = gmailProvider;
        this.ingestionHandler  = ingestionHandler;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public EmailMessage ingest(IngestEmailCommand command) {
        return ingestionHandler.ingest(command);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public int sync(InitialSyncCommand command) {
        EmailAccount account = accountRepository.findById(command.emailAccountId())
                .orElseThrow(() -> new EntityNotFoundException(
                        EmailAccount.class, command.emailAccountId()));

        String syncCorrelationId = CorrelationIdHolder.generateNew();

        List<String> messageIds;
        String latestHistoryId = null;

        if (account.getSyncState().historyId() == null) {
            Instant afterDate = Instant.now().minus(command.daysBack(), ChronoUnit.DAYS);
            messageIds = gmailProvider.listMessageIdsSince(account, afterDate);
        } else {
            HistoryDeltaResult deltaResult = gmailProvider.fetchHistoryDelta(
                    account, account.getSyncState().historyId());
            messageIds = deltaResult.records().stream()
                    .flatMap(r -> r.addedMessageIds().stream())
                    .distinct()
                    .toList();
            latestHistoryId = deltaResult.latestHistoryId();
        }

        List<String> failedIds = new ArrayList<>();
        List<String> successfulIds = new ArrayList<>();

        for (String gmailMessageId : messageIds) {
            try {
                IngestEmailCommand ingestCmd = new IngestEmailCommand(
                        command.emailAccountId(), gmailMessageId, syncCorrelationId);
                ingestionHandler.ingest(ingestCmd);
                successfulIds.add(gmailMessageId);
            } catch (Exception e) {
                log.error("Failed to ingest message {} for account {}",
                        gmailMessageId, command.emailAccountId(), e);
                failedIds.add(gmailMessageId);
            }
        }

        if (!failedIds.isEmpty()) {
            log.warn("{}/{} messages failed during sync for account {}",
                    failedIds.size(), messageIds.size(), command.emailAccountId());
        }

        if (latestHistoryId != null) {
            account.updateSyncState(account.getSyncState().withUpdatedHistoryId(latestHistoryId));
            accountRepository.save(account);
        }

        return successfulIds.size();
    }
}
