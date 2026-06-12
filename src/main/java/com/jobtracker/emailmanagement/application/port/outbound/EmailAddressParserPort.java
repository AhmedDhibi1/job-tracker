package com.jobtracker.emailmanagement.application.port.outbound;

import com.jobtracker.shared.domain.valueobject.EmailAddress;
import java.util.List;

public interface EmailAddressParserPort {
    List<EmailAddress> parse(String headerValue);
    EmailAddress parseSingle(String headerValue);
}
