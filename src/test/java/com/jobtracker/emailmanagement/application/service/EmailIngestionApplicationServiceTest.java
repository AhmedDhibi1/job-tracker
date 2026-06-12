package com.jobtracker.emailmanagement.application.service;

import com.jobtracker.emailmanagement.application.command.IngestEmailCommand;
import com.jobtracker.emailmanagement.application.command.InitialSyncCommand;
import com.jobtracker.emailmanagement.application.dto.EmailHistoryDelta;
import com.jobtracker.emailmanagement.application.dto.HistoryDeltaResult;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.model.*;
import com.jobtracker.shared.application.exception.EntityNotFoundException;
import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailIngestionApplicationServiceTest {

    @Mock EmailAccountRepository accountRepository;
    @Mock GmailProviderPort gmailProvider;
    @Mock SingleMessageIngestionHandler ingestionHandler;
    @InjectMocks EmailIngestionApplicationService service;

    private final UUID accountId = UUID.randomUUID();
    private final EmailAccount account = EmailAccount.create(
            accountId, new EmailAddress("user@example.com"), "User", false,
            new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)));

    @Test
    void ingest_delegatesToHandler() {
        IngestEmailCommand cmd = new IngestEmailCommand(accountId, "msg1", "corr-1");
        EmailMessage expected = EmailMessage.create(UUID.randomUUID(), "msg1", "t1", accountId,
                new EmailAddress("s@x.com"), List.of(), new CompanyDomain("x.com"), EmailDirection.INBOUND,
                Instant.now(), "Subj", "body", null, List.of());
        when(ingestionHandler.ingest(cmd)).thenReturn(expected);

        EmailMessage result = service.ingest(cmd);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void sync_throwsWhenAccountNotFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.sync(new InitialSyncCommand(accountId, 30)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void sync_usesListMessageIds_whenNoHistoryId() {
        InitialSyncCommand cmd = new InitialSyncCommand(accountId, 30);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(gmailProvider.listMessageIdsSince(any(), any())).thenReturn(List.of("msg1", "msg2"));
        when(ingestionHandler.ingest(any())).thenReturn(mock(EmailMessage.class));

        int result = service.sync(cmd);

        assertThat(result).isEqualTo(2);
        verify(gmailProvider).listMessageIdsSince(eq(account), any());
        verify(ingestionHandler, times(2)).ingest(any());
    }

    @Test
    void sync_usesHistoryDelta_whenHistoryIdExists() {
        EmailAccount accountWithHistory = EmailAccount.reconstitute(
                accountId, new EmailAddress("user@example.com"), "User", false,
                new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)),
                new SyncState("h1", null, false), 0, true, 0L);
        InitialSyncCommand cmd = new InitialSyncCommand(accountId, 30);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountWithHistory));
        EmailHistoryDelta delta = new EmailHistoryDelta("h2", List.of("msg1", "msg2"));
        when(gmailProvider.fetchHistoryDelta(accountWithHistory, "h1"))
                .thenReturn(new HistoryDeltaResult(List.of(delta), "h2"));
        when(ingestionHandler.ingest(any())).thenReturn(mock(EmailMessage.class));

        int result = service.sync(cmd);

        assertThat(result).isEqualTo(2);
        verify(gmailProvider, never()).listMessageIdsSince(any(), any());
        verify(ingestionHandler, times(2)).ingest(any());
    }

    @Test
    void sync_countsOnlySuccessfulIngestions() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(gmailProvider.listMessageIdsSince(any(), any())).thenReturn(List.of("msg1", "msg2", "msg3"));
        when(ingestionHandler.ingest(any()))
                .thenReturn(mock(EmailMessage.class))
                .thenThrow(new RuntimeException("ingestion failed"))
                .thenReturn(mock(EmailMessage.class));

        int result = service.sync(new InitialSyncCommand(accountId, 30));

        assertThat(result).isEqualTo(2);
    }

    @Test
    void sync_updatesHistoryId_whenDeltaSync() {
        EmailAccount accountWithHistory = EmailAccount.reconstitute(
                accountId, new EmailAddress("user@example.com"), "User", false,
                new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)),
                new SyncState("h1", null, false), 0, true, 0L);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountWithHistory));
        EmailHistoryDelta delta = new EmailHistoryDelta("h2", List.of("msg1"));
        when(gmailProvider.fetchHistoryDelta(accountWithHistory, "h1"))
                .thenReturn(new HistoryDeltaResult(List.of(delta), "h2"));
        when(ingestionHandler.ingest(any())).thenReturn(mock(EmailMessage.class));

        service.sync(new InitialSyncCommand(accountId, 30));

        assertThat(accountWithHistory.getSyncState().historyId()).isEqualTo("h2");
        verify(accountRepository).save(accountWithHistory);
    }

    @Test
    void sync_doesNotUpdateHistoryId_whenListMessageIdsUsed() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(gmailProvider.listMessageIdsSince(any(), any())).thenReturn(List.of("msg1"));
        when(ingestionHandler.ingest(any())).thenReturn(mock(EmailMessage.class));

        service.sync(new InitialSyncCommand(accountId, 30));

        assertThat(account.getSyncState().historyId()).isNull();
        verify(accountRepository, never()).save(any());
    }

    @Test
    void sync_handlesEmptyMessageList() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(gmailProvider.listMessageIdsSince(any(), any())).thenReturn(List.of());

        int result = service.sync(new InitialSyncCommand(accountId, 30));

        assertThat(result).isZero();
        verify(ingestionHandler, never()).ingest(any());
    }

    @Test
    void sync_distinctMessageIdsFromHistory() {
        EmailAccount accountWithHistory = EmailAccount.reconstitute(
                accountId, new EmailAddress("user@example.com"), "User", false,
                new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)),
                new SyncState("h1", null, false), 0, true, 0L);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountWithHistory));
        EmailHistoryDelta delta1 = new EmailHistoryDelta("h2", List.of("msg1", "msg2"));
        EmailHistoryDelta delta2 = new EmailHistoryDelta("h3", List.of("msg2", "msg3"));
        when(gmailProvider.fetchHistoryDelta(accountWithHistory, "h1"))
                .thenReturn(new HistoryDeltaResult(List.of(delta1, delta2), "h3"));
        when(ingestionHandler.ingest(any())).thenReturn(mock(EmailMessage.class));

        int result = service.sync(new InitialSyncCommand(accountId, 30));

        assertThat(result).isEqualTo(3);
        verify(ingestionHandler, times(3)).ingest(any());
    }

    @Test
    void sync_usesCorrectDateRange() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(gmailProvider.listMessageIdsSince(any(), any())).thenReturn(List.of());
        service.sync(new InitialSyncCommand(accountId, 7));
        verify(gmailProvider).listMessageIdsSince(eq(account), any());
    }
}
