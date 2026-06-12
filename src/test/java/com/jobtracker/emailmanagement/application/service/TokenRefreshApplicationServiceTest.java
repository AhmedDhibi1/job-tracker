package com.jobtracker.emailmanagement.application.service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.EmailEncryptionPort;
import com.jobtracker.emailmanagement.domain.exception.TokenRefreshFailedException;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.shared.application.exception.EntityNotFoundException;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRefreshApplicationServiceTest {

    @Mock EmailAccountRepository accountRepository;
    @Mock EmailEncryptionPort encryptionPort;

    private TokenRefreshApplicationService service;

    private final UUID accountId = UUID.randomUUID();
    private final OAuthTokenPair tokens = new OAuthTokenPair("encAccess", "encRefresh", Instant.now().plusSeconds(3600));
    private final EmailAccount account = EmailAccount.create(
            accountId, new EmailAddress("user@example.com"), "User", false, tokens);

    @BeforeEach
    void setUp() {
        service = new TokenRefreshApplicationService(accountRepository, encryptionPort,
                "test-client-id", "test-client-secret");
    }

    @Test
    void refreshAndPersist_throwsWhenAccountNotFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.refreshAndPersist(accountId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void refreshAndPersist_throwsTokenRefreshFailed_whenGoogleRefreshFails() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(encryptionPort.decrypt("encRefresh")).thenReturn("decryptedRefresh");

        assertThatThrownBy(() -> service.refreshAndPersist(accountId))
                .isInstanceOf(TokenRefreshFailedException.class);
    }

    @Test
    void refreshAndPersist_doesNotCallEncrypt_whenRefreshFails() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(encryptionPort.decrypt("encRefresh")).thenReturn("decryptedRefresh");

        assertThatThrownBy(() -> service.refreshAndPersist(accountId))
                .isInstanceOf(TokenRefreshFailedException.class);

        verify(encryptionPort, never()).encrypt(any());
    }

    @Test
    void refreshAndPersist_successfullyRefreshesToken() throws Exception {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(encryptionPort.decrypt("encRefresh")).thenReturn("decryptedRefresh");
        when(encryptionPort.encrypt("newAccessToken")).thenReturn("encryptedNewAccessToken");
        when(accountRepository.save(any(EmailAccount.class))).thenReturn(account);

        try (MockedStatic<UserCredentials> ucMock = mockStatic(UserCredentials.class)) {
            UserCredentials.Builder builder = mock(UserCredentials.Builder.class);
            UserCredentials credentials = mock(UserCredentials.class);
            AccessToken accessToken = mock(AccessToken.class);

            ucMock.when(UserCredentials::newBuilder).thenReturn(builder);
            when(builder.setClientId("test-client-id")).thenReturn(builder);
            when(builder.setClientSecret("test-client-secret")).thenReturn(builder);
            when(builder.setRefreshToken("decryptedRefresh")).thenReturn(builder);
            when(builder.build()).thenReturn(credentials);

            doNothing().when(credentials).refreshIfExpired();
            when(credentials.getAccessToken()).thenReturn(accessToken);
            when(accessToken.getTokenValue()).thenReturn("newAccessToken");
            when(accessToken.getExpirationTime()).thenReturn(
                    Date.from(Instant.now().plusSeconds(3600)));

            EmailAccount result = service.refreshAndPersist(accountId);

            assertThat(result).isNotNull();
            verify(accountRepository).save(any(EmailAccount.class));
        }
    }
}
