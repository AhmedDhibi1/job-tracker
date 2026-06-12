package com.jobtracker.emailmanagement.application.service;

import com.jobtracker.emailmanagement.application.command.IngestEmailCommand;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.EmailMessageRepository;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.event.EmailIngestedEvent;
import com.jobtracker.emailmanagement.domain.model.*;
import com.jobtracker.emailmanagement.domain.service.EmailParserDomainService;
import com.jobtracker.shared.application.exception.EntityNotFoundException;
import com.jobtracker.shared.application.port.outbound.ApplicationEventPublisherPort;
import com.jobtracker.shared.domain.valueobject.CompanyDomain;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class SingleMessageIngestionHandlerTest {

    @Mock EmailAccountRepository accountRepository;
    @Mock EmailMessageRepository messageRepository;
    @Mock GmailProviderPort gmailProvider;
    @Mock EmailParserDomainService emailParser;
    @Mock ApplicationEventPublisherPort eventPublisher;
    @InjectMocks SingleMessageIngestionHandler handler;

    @Captor ArgumentCaptor<EmailIngestedEvent> eventCaptor;

    private final UUID accountId = UUID.randomUUID();
    private final EmailAccount account = EmailAccount.create(
            accountId, new EmailAddress("user@example.com"), "User", false,
            new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)));
    private final IngestEmailCommand cmd = new IngestEmailCommand(accountId, "msg123", "corr-1");

    @Test
    void ingest_returnsExistingMessage_whenAlreadyExists() {
        EmailMessage existing = EmailMessage.create(UUID.randomUUID(), "msg123", "thread1", accountId,
                new EmailAddress("s@x.com"), List.of(), new CompanyDomain("x.com"),
                EmailDirection.INBOUND, Instant.now(), "Subj", "body", null, List.of());
        when(messageRepository.existsByGmailMessageId("msg123")).thenReturn(true);
        when(messageRepository.findByGmailMessageId("msg123")).thenReturn(Optional.of(existing));

        EmailMessage result = handler.ingest(cmd);

        assertThat(result).isSameAs(existing);
        verify(messageRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void ingest_throws_whenMessageExistsButCannotBeRetrieved() {
        when(messageRepository.existsByGmailMessageId("msg123")).thenReturn(true);
        when(messageRepository.findByGmailMessageId("msg123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.ingest(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exists but cannot be retrieved");
    }

    @Test
    void ingest_throws_whenAccountNotFound() {
        when(messageRepository.existsByGmailMessageId("msg123")).thenReturn(false);
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.ingest(cmd))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void ingest_throws_whenAccountDeactivated() {
        EmailAccount deactivated = EmailAccount.reconstitute(accountId, new EmailAddress("user@example.com"),
                "User", false, new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)),
                SyncState.initial(), 0, false, 0L);
        when(messageRepository.existsByGmailMessageId("msg123")).thenReturn(false);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(deactivated));

        assertThatThrownBy(() -> handler.ingest(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deactivated");
    }

    @Test
    void ingest_fetchesParsesSavesAndPublishes() {
        RawEmailInput raw = new RawEmailInput("msg123", "thread1", null, List.of(),
                Instant.now(), new EmailAddress("s@x.com"), List.of(new EmailAddress("user@example.com")),
                "Subj", "body", null);
        EmailMessage parsed = EmailMessage.create(UUID.randomUUID(), "msg123", "thread1", accountId,
                new EmailAddress("s@x.com"), List.of(), new CompanyDomain("x.com"),
                EmailDirection.INBOUND, Instant.now(), "Subj", "body", null, List.of());

        when(messageRepository.existsByGmailMessageId("msg123")).thenReturn(false);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(gmailProvider.fetchMessage(account, "msg123")).thenReturn(raw);
        when(emailParser.parse(raw, accountId, account.getEmailAddress())).thenReturn(parsed);
        when(messageRepository.save(parsed)).thenReturn(parsed);

        EmailMessage result = handler.ingest(cmd);

        assertThat(result).isSameAs(parsed);
        verify(messageRepository).save(parsed);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEmailMessageId()).isEqualTo(parsed.getId());
        assertThat(eventCaptor.getValue().getGmailMessageId()).isEqualTo("msg123");
        assertThat(eventCaptor.getValue().getAccountId()).isEqualTo(accountId);
    }

    @Test
    void ingest_usesCorrelationIdFromCommand_whenProvided() {
        RawEmailInput raw = new RawEmailInput("msg123", "thread1", null, List.of(),
                Instant.now(), new EmailAddress("s@x.com"), List.of(new EmailAddress("user@example.com")),
                "Subj", "body", null);
        EmailMessage parsed = EmailMessage.create(UUID.randomUUID(), "msg123", "thread1", accountId,
                new EmailAddress("s@x.com"), List.of(), new CompanyDomain("x.com"),
                EmailDirection.INBOUND, Instant.now(), "Subj", "body", null, List.of());

        when(messageRepository.existsByGmailMessageId("msg123")).thenReturn(false);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(gmailProvider.fetchMessage(account, "msg123")).thenReturn(raw);
        when(emailParser.parse(raw, accountId, account.getEmailAddress())).thenReturn(parsed);
        when(messageRepository.save(parsed)).thenReturn(parsed);

        handler.ingest(cmd);

        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getCorrelationId()).isEqualTo("corr-1");
    }

    @Test
    void ingest_usesRandomCorrelationId_whenNotProvided() {
        IngestEmailCommand cmdNoCorr = new IngestEmailCommand(accountId, "msg123", null);
        RawEmailInput raw = new RawEmailInput("msg123", "thread1", null, List.of(),
                Instant.now(), new EmailAddress("s@x.com"), List.of(new EmailAddress("user@example.com")),
                "Subj", "body", null);
        EmailMessage parsed = EmailMessage.create(UUID.randomUUID(), "msg123", "thread1", accountId,
                new EmailAddress("s@x.com"), List.of(), new CompanyDomain("x.com"),
                EmailDirection.INBOUND, Instant.now(), "Subj", "body", null, List.of());

        when(messageRepository.existsByGmailMessageId("msg123")).thenReturn(false);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(gmailProvider.fetchMessage(account, "msg123")).thenReturn(raw);
        when(emailParser.parse(raw, accountId, account.getEmailAddress())).thenReturn(parsed);
        when(messageRepository.save(parsed)).thenReturn(parsed);

        handler.ingest(cmdNoCorr);

        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getCorrelationId()).isNotNull();
    }
}
