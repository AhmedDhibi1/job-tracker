package com.jobtracker.emailmanagement.application.service;

import com.jobtracker.emailmanagement.application.command.RegisterEmailAccountCommand;
import com.jobtracker.emailmanagement.application.port.inbound.DeactivateEmailAccountUseCase;
import com.jobtracker.emailmanagement.application.port.inbound.RegisterEmailAccountUseCase;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.EmailEncryptionPort;
import com.jobtracker.emailmanagement.domain.event.EmailAccountDeactivatedEvent;
import com.jobtracker.emailmanagement.domain.event.EmailAccountRegisteredEvent;
import com.jobtracker.emailmanagement.domain.exception.DuplicateEmailAccountException;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.emailmanagement.infrastructure.gmail.GmailPushSubscriptionManager;
import com.jobtracker.shared.application.exception.EntityNotFoundException;
import com.jobtracker.shared.application.port.outbound.ApplicationEventPublisherPort;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class EmailAccountApplicationService
        implements RegisterEmailAccountUseCase, DeactivateEmailAccountUseCase {

    private final EmailAccountRepository       accountRepository;
    private final EmailEncryptionPort          encryptionPort;
    private final ApplicationEventPublisherPort eventPublisher;
    private final GmailPushSubscriptionManager pushSubscriptionManager;

    public EmailAccountApplicationService(
            EmailAccountRepository accountRepository,
            EmailEncryptionPort encryptionPort,
            ApplicationEventPublisherPort eventPublisher,
            GmailPushSubscriptionManager pushSubscriptionManager) {
        this.accountRepository = accountRepository;
        this.encryptionPort    = encryptionPort;
        this.eventPublisher    = eventPublisher;
        this.pushSubscriptionManager = pushSubscriptionManager;
    }

    @Override
    @Transactional
    public UUID register(RegisterEmailAccountCommand command) {
        EmailAddress emailAddress = new EmailAddress(command.emailAddress());

        if (accountRepository.existsByEmailAddress(emailAddress)) {
            throw new DuplicateEmailAccountException(emailAddress);
        }

        if (command.isPrimary()) {
            accountRepository.findAllActive().stream()
                    .filter(EmailAccount::isPrimary)
                    .findAny()
                    .ifPresent(existing -> {
                        throw new IllegalStateException("A primary account already exists: " + existing.getId());
                    });
        }

        String encryptedAccess  = encryptionPort.encrypt(command.rawAccessToken());
        String encryptedRefresh = encryptionPort.encrypt(command.rawRefreshToken());

        OAuthTokenPair tokens = new OAuthTokenPair(
                encryptedAccess, encryptedRefresh, command.tokenExpiry());

        EmailAccount account = EmailAccount.create(
                UUID.randomUUID(), emailAddress,
                command.displayName(), command.isPrimary(), tokens);

        accountRepository.save(account);

        eventPublisher.publish(new EmailAccountRegisteredEvent(
                account.getId(), emailAddress, correlationId()));

        return account.getId();
    }

    @Override
    @Transactional
    public void deactivate(UUID accountId, String reason) {
        EmailAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(EmailAccount.class, accountId));

        account.deactivate();
        accountRepository.save(account);

        pushSubscriptionManager.stopWatch(account);

        eventPublisher.publish(new EmailAccountDeactivatedEvent(
                accountId, account.getEmailAddress(), reason, correlationId()));
    }

    private static String correlationId() {
        String mdc = MDC.get("correlationId");
        return mdc != null ? mdc : UUID.randomUUID().toString();
    }
}