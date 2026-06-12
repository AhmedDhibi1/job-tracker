package com.jobtracker.emailmanagement.infrastructure.persistence.mapper;

import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.emailmanagement.domain.model.SyncState;
import com.jobtracker.emailmanagement.infrastructure.persistence.entity.EmailAccountJpaEntity;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailAccountPersistenceMapperTest {

    private final EmailAccountPersistenceMapper mapper = new EmailAccountPersistenceMapper();

    @Test
    void toDomain_mapsAllFields() {
        EmailAccountJpaEntity entity = new EmailAccountJpaEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setEmailAddress("test@example.com");
        entity.setDisplayName("Test User");
        entity.setPrimary(true);
        entity.setEncryptedAccessToken("encAccess");
        entity.setEncryptedRefreshToken("encRefresh");
        entity.setTokenExpiry(Instant.now().plusSeconds(3600));
        entity.setHistoryId("h1");
        entity.setWatchExpiration(Instant.now().plusSeconds(7200));
        entity.setPushEnabled(true);
        entity.setEmptyPollCount(3);
        entity.setActive(false);
        entity.setVersion(2L);

        EmailAccount domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getEmailAddress().value()).isEqualTo("test@example.com");
        assertThat(domain.getDisplayName()).isEqualTo("Test User");
        assertThat(domain.isPrimary()).isTrue();
        assertThat(domain.getOauthTokens().encryptedAccessToken()).isEqualTo("encAccess");
        assertThat(domain.getSyncState().historyId()).isEqualTo("h1");
        assertThat(domain.getEmptyPollCount()).isEqualTo(3);
        assertThat(domain.isActive()).isFalse();
        assertThat(domain.getVersion()).isEqualTo(2L);
    }

    @Test
    void toEntity_mapsAllFields() {
        UUID id = UUID.randomUUID();
        OAuthTokenPair tokens = new OAuthTokenPair("encAccess", "encRefresh", Instant.now().plusSeconds(3600));
        SyncState syncState = new SyncState("h1", Instant.now().plusSeconds(7200), true);
        EmailAccount domain = new EmailAccount(id, new EmailAddress("test@example.com"), "Test User",
                true, tokens, syncState, 3, false, 2L);

        EmailAccountJpaEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getEmailAddress()).isEqualTo("test@example.com");
        assertThat(entity.getDisplayName()).isEqualTo("Test User");
        assertThat(entity.isPrimary()).isTrue();
        assertThat(entity.getEncryptedAccessToken()).isEqualTo("encAccess");
        assertThat(entity.getHistoryId()).isEqualTo("h1");
        assertThat(entity.getEmptyPollCount()).isEqualTo(3);
        assertThat(entity.isActive()).isFalse();
        assertThat(entity.getVersion()).isEqualTo(2L);
    }

    @Test
    void toEntity_usesDefaultVersionWhenNull() {
        EmailAccount domain = EmailAccount.create(UUID.randomUUID(), new EmailAddress("test@example.com"),
                "Test", false, new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)));

        EmailAccountJpaEntity entity = mapper.toEntity(domain);

        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void roundTrip_preservesAllFields() {
        UUID id = UUID.randomUUID();
        OAuthTokenPair tokens = new OAuthTokenPair("encAccess", "encRefresh", Instant.now().plusSeconds(3600));
        SyncState syncState = new SyncState("h1", Instant.now().plusSeconds(7200), true);
        EmailAccount original = new EmailAccount(id, new EmailAddress("test@example.com"), "Test User",
                true, tokens, syncState, 5, false, 3L);

        EmailAccountJpaEntity entity = mapper.toEntity(original);
        EmailAccount restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getEmailAddress()).isEqualTo(original.getEmailAddress());
        assertThat(restored.getDisplayName()).isEqualTo(original.getDisplayName());
        assertThat(restored.isPrimary()).isEqualTo(original.isPrimary());
        assertThat(restored.getOauthTokens()).isEqualTo(original.getOauthTokens());
        assertThat(restored.getSyncState()).isEqualTo(original.getSyncState());
        assertThat(restored.getEmptyPollCount()).isEqualTo(original.getEmptyPollCount());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
        assertThat(restored.getVersion()).isEqualTo(original.getVersion());
    }

    @Test
    void toDomain_handlesNullWatchExpiration() {
        EmailAccountJpaEntity entity = new EmailAccountJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmailAddress("test@example.com");
        entity.setDisplayName("User");
        entity.setPrimary(false);
        entity.setEncryptedAccessToken("a");
        entity.setEncryptedRefreshToken("b");
        entity.setTokenExpiry(Instant.now().plusSeconds(3600));
        entity.setActive(true);
        entity.setVersion(0L);

        EmailAccount domain = mapper.toDomain(entity);

        assertThat(domain.getSyncState().watchExpiration()).isNull();
        assertThat(domain.getSyncState().pushEnabled()).isFalse();
    }

    @Test
    void toEntity_handlesNullVersionInDomain() {
        EmailAccount domain = EmailAccount.create(UUID.randomUUID(), new EmailAddress("test@example.com"),
                "Test", false, new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)));
        EmailAccountJpaEntity entity = mapper.toEntity(domain);
        assertThat(entity.getVersion()).isZero();
    }
}
