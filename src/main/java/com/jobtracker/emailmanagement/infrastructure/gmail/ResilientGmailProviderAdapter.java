package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.jobtracker.emailmanagement.application.dto.HistoryDeltaResult;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.RawEmailInput;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

@Component
public class ResilientGmailProviderAdapter implements GmailProviderPort {

    private static final Logger log = LoggerFactory.getLogger(ResilientGmailProviderAdapter.class);

    private final GmailProviderPort delegate;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;

    public ResilientGmailProviderAdapter(GmailProviderPort delegate) {
        this.delegate = delegate;

        this.retry = Retry.of("gmail-api", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryOnResult(result -> false)
                .retryExceptions(Throwable.class)
                .build());

        this.rateLimiter = RateLimiter.of("gmail-quota", RateLimiterConfig.custom()
                .limitForPeriod(250)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(5))
                .build());

        this.circuitBreaker = CircuitBreaker.of("gmail-api", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .build());

        this.retry.getEventPublisher().onRetry(event ->
                log.warn("Gmail API retry attempt {} for {}", event.getNumberOfRetryAttempts(), event.getName()));
        this.circuitBreaker.getEventPublisher().onStateTransition(event ->
                log.info("Gmail API circuit breaker state transition: {}", event.getStateTransition()));
    }

    @Override
    public RawEmailInput fetchMessage(EmailAccount account, String gmailMessageId) {
        return decorate(() -> delegate.fetchMessage(account, gmailMessageId));
    }

    @Override
    public HistoryDeltaResult fetchHistoryDelta(EmailAccount account, String fromHistoryId) {
        return decorate(() -> delegate.fetchHistoryDelta(account, fromHistoryId));
    }

    @Override
    public List<String> listMessageIdsSince(EmailAccount account, Instant afterDate) {
        return decorate(() -> delegate.listMessageIdsSince(account, afterDate));
    }

    @Override
    public WatchResult setupWatch(EmailAccount account) {
        return decorate(() -> delegate.setupWatch(account));
    }

    @Override
    public WatchResult renewWatch(EmailAccount account) {
        return decorate(() -> delegate.renewWatch(account));
    }

    private <T> T decorate(Supplier<T> supplier) {
        Supplier<T> decorated = Retry.decorateSupplier(retry, supplier);
        decorated = RateLimiter.decorateSupplier(rateLimiter, decorated);
        decorated = CircuitBreaker.decorateSupplier(circuitBreaker, decorated);
        return decorated.get();
    }
}
