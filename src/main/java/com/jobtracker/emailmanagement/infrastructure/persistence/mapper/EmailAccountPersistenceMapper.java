package com.jobtracker.emailmanagement.infrastructure.persistence.mapper;

import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.emailmanagement.domain.model.SyncState;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailAccountJpaEntity;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.springframework.stereotype.Component;

@Component
public class EmailAccountPersistenceMapper {

    public EmailAccount toDomain(EmailAccountJpaEntity entity) {
        OAuthTokenPair tokens = new OAuthTokenPair(
                entity.getEncryptedAccessToken(),
                entity.getEncryptedRefreshToken(),
                entity.getTokenExpiresAt()
        );

        SyncState syncState = new SyncState(
                entity.getHistoryId(),
                entity.getWatchExpiration(),
                entity.isPushEnabled()
        );

        return new EmailAccount(
                entity.getId(),
                new EmailAddress(entity.getEmailAddress()),
                entity.getDisplayName(),
                entity.isPrimary(),
                tokens,
                syncState,
                entity.getEmptyPollCount(),
                entity.isActive(),
                entity.getVersion()
        );
    }

    public EmailAccountJpaEntity toEntity(EmailAccount domain) {
        EmailAccountJpaEntity entity = new EmailAccountJpaEntity();
        entity.setId(domain.getId());
        entity.setEmailAddress(domain.getEmailAddress().value());
        entity.setDisplayName(domain.getDisplayName());
        entity.setPrimary(domain.isPrimary());
        entity.setEncryptedAccessToken(domain.getOauthTokens().encryptedAccessToken());
        entity.setEncryptedRefreshToken(domain.getOauthTokens().encryptedRefreshToken());
        entity.setTokenExpiresAt(domain.getOauthTokens().tokenExpiry());
        entity.setHistoryId(domain.getSyncState().historyId());
        entity.setWatchExpiration(domain.getSyncState().watchExpiration());
        entity.setPushEnabled(domain.getSyncState().pushEnabled());
        entity.setEmptyPollCount(domain.getEmptyPollCount());
        entity.setActive(domain.isActive());
        entity.setVersion(domain.getVersion() != null ? domain.getVersion() : 0L);
        return entity;
    }
}
