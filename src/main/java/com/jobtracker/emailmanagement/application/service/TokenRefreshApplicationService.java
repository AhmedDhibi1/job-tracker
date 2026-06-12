package com.jobtracker.emailmanagement.application.service;

import com.jobtracker.emailmanagement.application.port.inbound.RefreshTokenUseCase;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.EmailEncryptionPort;
import com.jobtracker.emailmanagement.domain.exception.TokenRefreshFailedException;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.shared.application.exception.EntityNotFoundException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class TokenRefreshApplicationService implements RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshApplicationService.class);

    private final EmailAccountRepository accountRepository;
    private final EmailEncryptionPort encryptionPort;
    private final String googleClientId;
    private final String googleClientSecret;

    public TokenRefreshApplicationService(
            EmailAccountRepository accountRepository,
            EmailEncryptionPort encryptionPort,
            @Value("${gmail.oauth.client-id}") String googleClientId,
            @Value("${gmail.oauth.client-secret}") String googleClientSecret) {
        this.accountRepository = accountRepository;
        this.encryptionPort = encryptionPort;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
    }

    @Override
    @Transactional
    public EmailAccount refreshAndPersist(UUID accountId) {
        EmailAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(EmailAccount.class, accountId));

        String decryptedRefresh = encryptionPort.decrypt(account.getOauthTokens().encryptedRefreshToken());

        try {
            GoogleCredentials credentials = UserCredentials.newBuilder()
                    .setClientId(googleClientId)
                    .setClientSecret(googleClientSecret)
                    .setRefreshToken(decryptedRefresh)
                    .build();

            credentials.refreshIfExpired();
            String newAccessToken = credentials.getAccessToken().getTokenValue();
            Instant newExpiry = Instant.ofEpochMilli(
                    credentials.getAccessToken().getExpirationTime().getTime());

            String encryptedAccess = encryptionPort.encrypt(newAccessToken);

            OAuthTokenPair newTokens = new OAuthTokenPair(
                    encryptedAccess,
                    account.getOauthTokens().encryptedRefreshToken(),
                    newExpiry);

            account.updateTokens(newTokens);
            accountRepository.save(account);

            log.info("Successfully refreshed and persisted access token for account {}", accountId);
            return account;

        } catch (IOException e) {
            log.error("Token refresh failed for account {}", accountId, e);
            throw new TokenRefreshFailedException(accountId, e);
        }
    }
}
