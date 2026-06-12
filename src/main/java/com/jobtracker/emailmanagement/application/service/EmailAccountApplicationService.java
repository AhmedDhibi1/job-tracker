package com.jobtracker.emailmanagement.application.service;

import com.jobtracker.emailmanagement.application.command.RegisterEmailAccountCommand;
import com.jobtracker.emailmanagement.application.port.inbound.DeactivateEmailAccountUseCase;
import com.jobtracker.emailmanagement.application.port.inbound.RegisterEmailAccountUseCase;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.PushSubscriptionPort;
import com.jobtracker.emailmanagement.domain.event.EmailAccountDeactivatedEvent;
import com.jobtracker.emailmanagement.domain.event.EmailAccountRegisteredEvent;
import com.jobtracker.emailmanagement.domain.exception.DuplicateEmailAccountException;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.shared.CorrelationIdHolder;
import com.jobtracker.shared.application.exception.ConcurrentModificationException;
import com.jobtracker.shared.application.exception.EntityNotFoundException;
import com.jobtracker.shared.application.port.outbound.ApplicationEventPublisherPort;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class EmailAccountApplicationService
        implements RegisterEmailAccountUseCase, DeactivateEmailAccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(EmailAccountApplicationService.class);

    private final EmailAccountRepository       accountRepository;
    private final PushSubscriptionPort         pushSubscriptionPort;
    private final ApplicationEventPublisherPort eventPublisher;

    public EmailAccountApplicationService(
            EmailAccountRepository accountRepository,
            PushSubscriptionPort pushSubscriptionPort,
            ApplicationEventPublisherPort eventPublisher) {
        this.accountRepository     = accountRepository;
        this.pushSubscriptionPort  = pushSubscriptionPort;
        this.eventPublisher        = eventPublisher;
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

        OAuthTokenPair tokens = new OAuthTokenPair(
                command.encryptedAccessToken(), command.encryptedRefreshToken(), command.tokenExpiry());

        EmailAccount account = EmailAccount.create(
                UUID.randomUUID(), emailAddress,
                command.displayName(), command.isPrimary(), tokens);

        accountRepository.save(account);

        eventPublisher.publish(new EmailAccountRegisteredEvent(
                account.getId(), emailAddress, CorrelationIdHolder.current()));

        return account.getId();
    }

    @Override
    @Transactional
    public void deactivate(UUID accountId, String reason) {
        try {
            EmailAccount account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new EntityNotFoundException(EmailAccount.class, accountId));

            account.deactivate();
            account.updateSyncState(account.getSyncState().withWatchStopped());
            accountRepository.save(account);

            pushSubscriptionPort.stopWatch(account);

            eventPublisher.publish(new EmailAccountDeactivatedEvent(
                    accountId, account.getEmailAddress(), reason, CorrelationIdHolder.current()));

        } catch (OptimisticLockException e) {
            log.warn("Concurrent modification of account {}", accountId);
            throw new ConcurrentModificationException("EmailAccount", accountId.toString(), e);
        }
    }
}
