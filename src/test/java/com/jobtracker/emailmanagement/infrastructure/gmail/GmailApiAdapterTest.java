package com.jobtracker.emailmanagement.infrastructure.gmail;

import com.jobtracker.emailmanagement.application.dto.HistoryDeltaResult;
import com.jobtracker.emailmanagement.application.port.outbound.EmailAddressParserPort;
import com.jobtracker.emailmanagement.application.port.outbound.GmailProviderPort;
import com.jobtracker.emailmanagement.domain.model.EmailAccount;
import com.jobtracker.emailmanagement.domain.model.OAuthTokenPair;
import com.jobtracker.emailmanagement.domain.model.RawEmailInput;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.GmailHistoryRecord;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.HistoryDeltaInfraResult;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.RawGmailMessage;
import com.jobtracker.emailmanagement.infrastructure.gmail.model.RawGmailMessage.MimePart;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GmailApiAdapterTest {

    @Mock GmailMessageFetcher messageFetcher;
    @Mock GmailHistoryFetcher historyFetcher;
    @Mock GmailPushSubscriptionManager pushManager;
    @Mock EmailAddressParserPort addressParser;
    @InjectMocks GmailApiAdapter adapter;

    private final EmailAccount account = EmailAccount.create(
            UUID.randomUUID(), new EmailAddress("user@example.com"), "User", false,
            new OAuthTokenPair("a", "b", Instant.now().plusSeconds(3600)));

    @Test
    void fetchMessage_convertsRawGmailToRawEmailInput() {
        RawGmailMessage raw = new RawGmailMessage("msg1", "thread1", Map.of("to", "recip@x.com"),
                List.of(), Instant.now(), "sender@acme.com", List.of("recip@x.com"), "Subj", "body", null, List.of("user@example.com"));
        when(messageFetcher.fetch(account, "msg1")).thenReturn(raw);
        when(addressParser.parseSingle("sender@acme.com")).thenReturn(new EmailAddress("sender@acme.com"));
        when(addressParser.parse("recip@x.com")).thenReturn(List.of(new EmailAddress("recip@x.com")));

        RawEmailInput result = adapter.fetchMessage(account, "msg1");

        assertThat(result.gmailMessageId()).isEqualTo("msg1");
        assertThat(result.from().value()).isEqualTo("sender@acme.com");
        assertThat(result.to()).hasSize(1);
    }

    @Test
    void fetchHistoryDelta_convertsInfraToAppDto() {
        GmailHistoryRecord record = new GmailHistoryRecord("h2", List.of("msg1", "msg2"));
        HistoryDeltaInfraResult infra = new HistoryDeltaInfraResult(List.of(record), "h2");
        when(historyFetcher.fetchDelta(account, "h1")).thenReturn(infra);

        HistoryDeltaResult result = adapter.fetchHistoryDelta(account, "h1");

        assertThat(result.latestHistoryId()).isEqualTo("h2");
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).addedMessageIds()).containsExactly("msg1", "msg2");
    }

    @Test
    void listMessageIdsSince_delegatesToFetcher() {
        when(messageFetcher.listMessageIdsSince(account, Instant.EPOCH)).thenReturn(List.of("msg1"));

        List<String> result = adapter.listMessageIdsSince(account, Instant.EPOCH);

        assertThat(result).containsExactly("msg1");
    }

    @Test
    void setupWatch_delegatesToPushManager() {
        GmailProviderPort.WatchResult expected = new GmailProviderPort.WatchResult(Instant.now().plusSeconds(3600), "h1");
        when(pushManager.setupWatch(account)).thenReturn(expected);

        GmailProviderPort.WatchResult result = adapter.setupWatch(account);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void renewWatch_delegatesToPushManager() {
        GmailProviderPort.WatchResult expected = mock(GmailProviderPort.WatchResult.class);
        when(pushManager.renewWatch(account)).thenReturn(expected);

        GmailProviderPort.WatchResult result = adapter.renewWatch(account);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void fetchMessage_mapsNonEmptyParts() {
        List<MimePart> mimeParts = List.of(
                new MimePart("text/plain", null, "", "Hello", null, 0),
                new MimePart("application/pdf", "attachment", "doc.pdf", "PDF_DATA", "att123", 5000));
        RawGmailMessage raw = new RawGmailMessage("msg2", "thread1", Map.of("to", "r@x.com"),
                mimeParts, Instant.now(), "s@x.com", List.of("r@x.com"), "Subj", "body", null, null);
        when(messageFetcher.fetch(account, "msg2")).thenReturn(raw);
        when(addressParser.parseSingle("s@x.com")).thenReturn(new EmailAddress("s@x.com"));
        when(addressParser.parse("r@x.com")).thenReturn(List.of(new EmailAddress("r@x.com")));

        RawEmailInput result = adapter.fetchMessage(account, "msg2");

        assertThat(result.parts()).hasSize(2);
        assertThat(result.parts().get(0).mimeType()).isEqualTo("text/plain");
        assertThat(result.parts().get(0).filename()).isEmpty();
        assertThat(result.parts().get(1).mimeType()).isEqualTo("application/pdf");
        assertThat(result.parts().get(1).contentDisposition()).isEqualTo("attachment");
        assertThat(result.parts().get(1).filename()).isEqualTo("doc.pdf");
        assertThat(result.parts().get(1).gmailAttachmentId()).isEqualTo("att123");
        assertThat(result.parts().get(1).sizeBytes()).isEqualTo(5000);
    }
}
