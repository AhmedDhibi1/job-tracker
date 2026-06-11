package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    /** Buffer before expiry to trigger proactive refresh (5 minutes). */
    private static final long EXPIRY_BUFFER_SECONDS = 300;

    private final EmailEncryptionPort encryptionPort;
    private final NetHttpTransport httpTransport;
    private final Map<String, Gmail> clientCache = new ConcurrentHashMap<>();
    private final Map<UUID, Lock> accountLocks = new ConcurrentHashMap<>();

    private final String applicationName;
    private final String googleClientId;
    private final String googleClientSecret;

    public GmailClientFactory(
            EmailEncryptionPort encryptionPort,
            @Value("${google.oauth.client-id}") String googleClientId,
            @Value("${google.oauth.client-secret}") String googleClientSecret,
            @Value("${app.name:JobTracker/1.0}") String applicationName) {
        this.encryptionPort = encryptionPort;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.applicationName = applicationName;
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Failed to initialize Gmail HTTP transport", e);
        }
    }


    public Gmail getClient(EmailAccount account) {
        Lock lock = accountLocks.computeIfAbsent(account.getId(), k -> new ReentrantLock());
        lock.lock();
        try {
            String key = account.getId().toString();

            OAuthTokenPair tokens = account.getOauthTokens();
            Instant now = Instant.now();
            Instant refreshThreshold = tokens.tokenExpiry().minusSeconds(EXPIRY_BUFFER_SECONDS);

            if (now.isAfter(refreshThreshold)) {
                log.debug("Access token for account {} is near expiry; refreshing", account.getId());
                clientCache.remove(key);
                OAuthTokenPair newTokens = refreshTokens(account);
                clientCache.put(key, buildGmailClient(account, newTokens));
                return clientCache.get(key);
            }

            return clientCache.computeIfAbsent(key, k -> buildGmailClient(account, tokens));
        } finally {
            lock.unlock();
        }
    }

    public OAuthTokenPair refreshTokensIfExpired(EmailAccount account) {
        OAuthTokenPair tokens = account.getOauthTokens();
        Instant refreshThreshold = tokens.tokenExpiry().minusSeconds(EXPIRY_BUFFER_SECONDS);
        if (Instant.now().isAfter(refreshThreshold)) {
            return refreshTokens(account);
        }
        return tokens;
    }


    public void invalidateCache(UUID accountId) {
        Gmail removed = clientCache.remove(accountId.toString());
        if (removed != null) {
            log.debug("Invalidated Gmail client cache for account {}", accountId);
        }
    }

    private OAuthTokenPair refreshTokens(EmailAccount account) {
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
