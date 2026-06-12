package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.jobtracker.emailmanagement.application.dto.HistoryDeltaResult;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.emailmanagement.domain.model.RawEmailInput;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilientGmailProviderAdapterTest {

    @Mock GmailProviderPort delegate;
    @InjectMocks ResilientGmailProviderAdapter adapter;

    private final EmailAccount account = EmailAccount.create(
            UUID.randomUUID(), new EmailAddress("user@example.com"), "User", false,
            new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)));

    @Test
    void fetchMessage_decoratesDelegate() {
        RawEmailInput expected = mock(RawEmailInput.class);
        when(delegate.fetchMessage(account, "msg1")).thenReturn(expected);

        RawEmailInput result = adapter.fetchMessage(account, "msg1");

        assertThat(result).isSameAs(expected);
    }

    @Test
    void fetchHistoryDelta_decoratesDelegate() {
        HistoryDeltaResult expected = mock(HistoryDeltaResult.class);
        when(delegate.fetchHistoryDelta(account, "h1")).thenReturn(expected);

        HistoryDeltaResult result = adapter.fetchHistoryDelta(account, "h1");

        assertThat(result).isSameAs(expected);
    }

    @Test
    void listMessageIdsSince_decoratesDelegate() {
        when(delegate.listMessageIdsSince(account, Instant.EPOCH)).thenReturn(List.of("msg1"));

        List<String> result = adapter.listMessageIdsSince(account, Instant.EPOCH);

        assertThat(result).containsExactly("msg1");
    }

    @Test
    void setupWatch_decoratesDelegate() {
        GmailProviderPort.WatchResult expected = mock(GmailProviderPort.WatchResult.class);
        when(delegate.setupWatch(account)).thenReturn(expected);

        GmailProviderPort.WatchResult result = adapter.setupWatch(account);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void renewWatch_decoratesDelegate() {
        GmailProviderPort.WatchResult expected = mock(GmailProviderPort.WatchResult.class);
        when(delegate.renewWatch(account)).thenReturn(expected);

        GmailProviderPort.WatchResult result = adapter.renewWatch(account);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void retry_firesOnFailure() {
        when(delegate.fetchMessage(account, "msg1")).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> adapter.fetchMessage(account, "msg1"))
                .isInstanceOf(RuntimeException.class);
    }
}
