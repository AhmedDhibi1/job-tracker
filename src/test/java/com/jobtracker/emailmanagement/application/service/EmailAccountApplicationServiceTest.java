package com.jobtracker.emailmanagement.application.service;

import com.jobtracker.emailmanagement.application.command.RegisterEmailAccountCommand;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.PushSubscriptionPort;
import com.jobtracker.emailmanagement.domain.event.EmailAccountDeactivatedEvent;
import com.jobtracker.emailmanagement.domain.event.EmailAccountRegisteredEvent;
import com.jobtracker.emailmanagement.domain.exception.DuplicateEmailAccountException;
import com.jobtracker.emailmanagement.domain.exception.PrimaryAccountAlreadyExistsException;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.emailmanagement.domain.model.SyncState;
import com.jobtracker.shared.application.port.outbound.ApplicationEventPublisherPort;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailAccountApplicationServiceTest {

    @Mock EmailAccountRepository accountRepository;
    @Mock PushSubscriptionPort pushSubscriptionPort;
    @Mock ApplicationEventPublisherPort eventPublisher;
    @InjectMocks EmailAccountApplicationService service;

    @Captor ArgumentCaptor<EmailAccountRegisteredEvent> registeredEventCaptor;

    private final RegisterEmailAccountCommand cmd = new RegisterEmailAccountCommand(
            "test@example.com", "Test User", false,
            "encAccess", "encRefresh", Instant.now().plusSeconds(3600));

    @Test
    void register_rejectsDuplicateEmail() {
        when(accountRepository.existsByEmailAddress(any())).thenReturn(true);
        assertThatThrownBy(() -> service.register(cmd))
                .isInstanceOf(DuplicateEmailAccountException.class);
    }

    @Test
    void register_rejectsDuplicatePrimary() {
        when(accountRepository.existsByIsPrimaryTrue()).thenReturn(true);
        RegisterEmailAccountCommand primaryCmd = new RegisterEmailAccountCommand(
                "primary@example.com", "Primary", true,
                "encAccess", "encRefresh", Instant.now().plusSeconds(3600));
        assertThatThrownBy(() -> service.register(primaryCmd))
                .isInstanceOf(PrimaryAccountAlreadyExistsException.class);
    }

    @Test
    void register_savesAndPublishesEvent() {
        when(accountRepository.existsByEmailAddress(any())).thenReturn(false);
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UUID accountId = service.register(cmd);

        assertThat(accountId).isNotNull();
        verify(accountRepository).save(any());
        verify(eventPublisher).publish(registeredEventCaptor.capture());
        assertThat(registeredEventCaptor.getValue().getEmailAddress().value())
                .isEqualTo("test@example.com");
    }

    @Test
    void deactivate_savesAccountAndPublishesEvent() {
        UUID accountId = UUID.randomUUID();
        EmailAccount account = EmailAccount.create(
                accountId, new EmailAddress("test@example.com"), "Test", false,
                new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        service.deactivate(accountId, "Testing");

        assertThat(account.isActive()).isFalse();
        verify(accountRepository).save(account);
        verify(eventPublisher).publish(any(EmailAccountDeactivatedEvent.class));
    }
}
