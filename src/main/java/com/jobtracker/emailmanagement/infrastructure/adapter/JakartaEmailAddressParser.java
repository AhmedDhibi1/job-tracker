package com.jobtracker.emailmanagement.infrastructure.adapter;

import com.jobtracker.emailmanagement.application.port.outbound.EmailAddressParserPort;
import com.jobtracker.shared.domain.valueobject.EmailAddress;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class JakartaEmailAddressParser implements EmailAddressParserPort {

    private static final Logger log = LoggerFactory.getLogger(JakartaEmailAddressParser.class);

    @Override
    public List<EmailAddress> parse(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return List.of();
        }
        try {
            return Arrays.stream(InternetAddress.parse(headerValue))
                    .map(InternetAddress::getAddress)
                    .filter(Objects::nonNull)
                    .map(EmailAddress::new)
                    .toList();
        } catch (AddressException e) {
            log.warn("Failed to parse address list '{}', falling back to split", headerValue);
            return Arrays.stream(headerValue.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(EmailAddress::new)
                    .toList();
        }
    }

    @Override
    public EmailAddress parseSingle(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new IllegalArgumentException("headerValue must not be null or blank");
        }
        try {
            InternetAddress[] addresses = InternetAddress.parse(headerValue);
            return new EmailAddress(addresses[0].getAddress());
        } catch (AddressException e) {
            throw new IllegalArgumentException("Cannot parse email address: " + headerValue, e);
        }
    }
}
