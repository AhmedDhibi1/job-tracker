package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAccountRepository;
import com.jobtracker.emailmanagement.application.port.outbound.EmailEncryptionPort;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.emailmanagement.domain.exception.TokenRefreshFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class GmailClientFactory {

    private static final Logger log = LoggerFactory.getLogger(GmailClientFactory.class);

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(
            GmailScopes.GMAIL_READONLY,
            GmailScopes.GMAIL_METADATA,
            GmailScopes.GMAIL_MODIFY
    );

    private static final long EXPIRY_BUFFER_SECONDS = 300;

    private final EmailEncryptionPort encryptionPort;
    private final EmailAccountRepository accountRepository;
    private final NetHttpTransport httpTransport;
    private final Cache<String, Gmail> clientCache;
    private final Cache<UUID, Lock> accountLocks;

    private final String applicationName;
    private final String googleClientId;
    private final String googleClientSecret;

    public GmailClientFactory(
            EmailEncryptionPort encryptionPort,
            EmailAccountRepository accountRepository,
            @Value("${gmail.oauth.client-id}") String googleClientId,
            @Value("${gmail.oauth.client-secret}") String googleClientSecret,
            @Value("${app.name:JobTracker/1.0}") String applicationName) {
        this.encryptionPort = encryptionPort;
        this.accountRepository = accountRepository;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.applicationName = applicationName;
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Failed to initialize Gmail HTTP transport", e);
        }

        this.clientCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000)
                .removalListener((String key, Gmail value, RemovalCause cause) -> {
                    if (cause.wasEvicted()) {
                        log.debug("Evicted Gmail client from cache for key {}", key);
                    }
                })
                .build();

        this.accountLocks = Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .<UUID, Lock>build();
    }

    public Gmail getClient(EmailAccount account) {
        Lock lock = accountLocks.get(account.getId(), k -> new ReentrantLock());
        lock.lock();
        try {
            String key = account.getId().toString();

            OAuthTokenPair tokens = account.getOauthTokens();
            Instant now = Instant.now();
            Instant refreshThreshold = tokens.tokenExpiry().minusSeconds(EXPIRY_BUFFER_SECONDS);

            if (now.isAfter(refreshThreshold)) {
                log.debug("Access token for account {} is near expiry; refreshing", account.getId());
                clientCache.invalidate(key);
                OAuthTokenPair newTokens = refreshAndPersistTokens(account);
                return clientCache.get(key, k -> buildGmailClient(account, newTokens));
            }

            return clientCache.get(key, k -> buildGmailClient(account, tokens));
        } finally {
            lock.unlock();
        }
    }

    public OAuthTokenPair refreshTokensIfExpired(EmailAccount account) {
        OAuthTokenPair tokens = account.getOauthTokens();
        Instant refreshThreshold = tokens.tokenExpiry().minusSeconds(EXPIRY_BUFFER_SECONDS);
        if (Instant.now().isAfter(refreshThreshold)) {
            return refreshAndPersistTokens(account);
        }
        return tokens;
    }

    public void invalidateCache(UUID accountId) {
        clientCache.invalidate(accountId.toString());
        accountLocks.invalidate(accountId);
        log.debug("Invalidated Gmail client cache and locks for account {}", accountId);
    }

    private OAuthTokenPair refreshAndPersistTokens(EmailAccount account) {
        OAuthTokenPair newTokens = doRefreshTokens(account);
        account.updateTokens(newTokens);
        accountRepository.save(account);
        log.info("Refreshed and persisted access tokens for account {}", account.getId());
        return newTokens;
    }

    private OAuthTokenPair doRefreshTokens(EmailAccount account) {
        String decryptedRefresh = encryptionPort.decrypt(account.getOauthTokens().encryptedRefreshToken());

        try {
            GoogleCredentials credentials = UserCredentials.newBuilder()
                    .setClientId(googleClientId)
                    .setClientSecret(googleClientSecret)
                    .setRefreshToken(decryptedRefresh)
                    .build();

            credentials.refreshIfExpired();
            String newAccessToken = credentials.getAccessToken().getTokenValue();
            Instant newExpiry = Instant.ofEpochMilli(credentials.getAccessToken().getExpirationTime().getTime());

            String encryptedAccess = encryptionPort.encrypt(newAccessToken);

            log.info("Successfully refreshed access token for account {}", account.getId());
            return new OAuthTokenPair(encryptedAccess, account.getOauthTokens().encryptedRefreshToken(), newExpiry);

        } catch (IOException e) {
            log.error("Token refresh failed for account {}: {}", account.getId(), e.getMessage());
            throw new TokenRefreshFailedException(account.getId(), e);
        }
    }

    private Gmail buildGmailClient(EmailAccount account, OAuthTokenPair tokens) {
        String decryptedAccess = encryptionPort.decrypt(tokens.encryptedAccessToken());
        String decryptedRefresh = encryptionPort.decrypt(tokens.encryptedRefreshToken());

        GoogleCredentials credentials = UserCredentials.newBuilder()
                .setClientId(googleClientId)
                .setClientSecret(googleClientSecret)
                .setAccessToken(new com.google.auth.oauth2.AccessToken(decryptedAccess, java.util.Date.from(tokens.tokenExpiry())))
                .setRefreshToken(decryptedRefresh)
                .build();

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Gmail.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName(applicationName)
                .build();
    }
}
