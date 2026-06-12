package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.jobtracker.emailmanagement.application.port.outbound.EmailEncryptionPort;
import com.jobtracker.emailmanagement.domain.exception.TokenExpiredException;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.emailmanagement.domain.model.SyncState;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class GmailClientFactoryTest {

    @Mock
    EmailEncryptionPort encryptionPort;

    private GmailClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new GmailClientFactory(encryptionPort, "TestApp/1.0");
    }

    @Test
    void getClient_throwsTokenExpired_whenTokenIsNearExpiry() {
        OAuthTokenPair nearExpiryTokens = new OAuthTokenPair(
                "encAccess", "encRefresh", Instant.now().plusSeconds(299));
        EmailAccount account = new EmailAccount(UUID.randomUUID(),
                new EmailAddress("test@example.com"), "Test", false,
                nearExpiryTokens, SyncState.initial(), 0, true, 0L);

        assertThatThrownBy(() -> factory.getClient(account))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    void getClient_throwsTokenExpired_whenTokenExactlyAtThreshold() {
        OAuthTokenPair atThresholdTokens = new OAuthTokenPair(
                "encAccess", "encRefresh", Instant.now().plusSeconds(300));
        EmailAccount account = new EmailAccount(UUID.randomUUID(),
                new EmailAddress("test@example.com"), "Test", false,
                atThresholdTokens, SyncState.initial(), 0, true, 0L);

        assertThatThrownBy(() -> factory.getClient(account))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    void invalidateCache_doesNotThrow() {
        UUID accountId = UUID.randomUUID();
        factory.invalidateCache(accountId);
    }
}
