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
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.jobtracker.emailmanagement.application.port.outbound.EmailEncryptionPort;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.emailmanagement.domain.exception.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Date;
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
            GmailScopes.GMAIL_METADATA
    );

    private static final long EXPIRY_BUFFER_SECONDS = 300;

    private final EmailEncryptionPort encryptionPort;
    private final NetHttpTransport httpTransport;
    private final Cache<String, Gmail> clientCache;
    private final Cache<UUID, Lock> accountLocks;

    private final String applicationName;

    public GmailClientFactory(
            EmailEncryptionPort encryptionPort,
            @Value("${app.name:JobTracker/1.0}") String applicationName) {
        this.encryptionPort = encryptionPort;
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
                log.debug("Access token for account {} is near expiry; signaling refresh needed", account.getId());
                clientCache.invalidate(key);
                throw new TokenExpiredException(account.getId());
            }

            return clientCache.get(key, k -> buildGmailClient(account, tokens));
        } finally {
            lock.unlock();
        }
    }

    public void invalidateCache(UUID accountId) {
        clientCache.invalidate(accountId.toString());
        accountLocks.invalidate(accountId);
        log.debug("Invalidated Gmail client cache and locks for account {}", accountId);
    }

    private Gmail buildGmailClient(EmailAccount account, OAuthTokenPair tokens) {
        HttpRequestInitializer requestInitializer = createRequestInitializer(account);

        return new Gmail.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName(applicationName)
                .build();
    }

    private HttpRequestInitializer createRequestInitializer(EmailAccount account) {
        return request -> {
            OAuthTokenPair tokenPair = account.getOauthTokens();
            String access = encryptionPort.decrypt(tokenPair.encryptedAccessToken());
            GoogleCredentials creds = GoogleCredentials.newBuilder()
                    .setAccessToken(new AccessToken(access,
                            Date.from(tokenPair.tokenExpiry())))
                    .build();
            new HttpCredentialsAdapter(creds).initialize(request);
        };
    }
}
